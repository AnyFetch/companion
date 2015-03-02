package com.anyfetch.companion.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.anyfetch.companion.ContextActivity;
import com.anyfetch.companion.R;
import com.anyfetch.companion.commons.api.builders.BaseRequestBuilder;
import com.anyfetch.companion.commons.api.builders.ContextualObject;
import com.anyfetch.companion.commons.api.builders.DocumentsListRequestBuilder;
import com.anyfetch.companion.commons.api.helpers.AnyfetchHtmlUtils;
import com.anyfetch.companion.commons.api.helpers.HtmlUtils;
import com.anyfetch.companion.commons.api.pojo.Document;
import com.anyfetch.companion.commons.api.pojo.DocumentsList;
import com.anyfetch.companion.commons.api.requests.GetDocumentsListRequest;
import com.anyfetch.companion.commons.ui.ImageHelper;
import com.anyfetch.companion.fragments.ContextFragment;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder for creating notification's contexts
 */
public class ContextNotificationBuilder {
    private static final int WEAR_CONTEXT_SIZE = 5;
    private final Context mContext;
    private final OkHttpClient mClient = new OkHttpClient();
    private DocumentsList mContextualObjectDocuments = null;
    private ContextualObject mContextualObject;
    private Set<String> mTailedEmails;
    private String mGroupKey;

    /**
     * Setups the notification builder
     *
     * @param context The android app context
     */
    public ContextNotificationBuilder(Context context) {
        mContext = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTailedEmails = prefs.getStringSet(BaseRequestBuilder.TAILED_EMAILS, new HashSet<String>());
    }

    /**
     * Sets the involved context object
     *
     * @param contextualObject A contextual object
     * @return A chainable builder
     */
    public ContextNotificationBuilder setContextualObject(ContextualObject contextualObject) {
        mContextualObject = contextualObject;
        mContextualObjectDocuments = null;

        mGroupKey = Integer.toString(contextualObject.getSearchQuery(mTailedEmails).hashCode());

        return this;
    }

    /**
     * Overrides the group key <strong>To be used after setting the contextual object</strong>
     *
     * @param groupKey The notification group key
     * @return A chainable builder
     */
    public ContextNotificationBuilder setGroupKey(String groupKey) {
        mGroupKey = groupKey;
        return this;
    }

    /**
     * Build a base notification.
     * This basic notification will later be enhanced, once we've loaded more contents asynchronously.
     *
     * @return a basic notification, suitable for later enhancement
     */
    protected NotificationCompat.Builder buildBaseNotification() {
        Intent viewIntent = new Intent(mContext, ContextActivity.class);
        viewIntent.putExtra(ContextFragment.ARG_CONTEXTUAL_OBJECT, mContextualObject);
        viewIntent.putExtra(ContextActivity.ORIGIN, "notification");


        PendingIntent viewPendingIntent = PendingIntent.getActivity(mContext, mContextualObject.getTitle().hashCode(), viewIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setLargeIcon(ImageHelper.toBitmap(mContextualObject.getIcon(mContext)))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(mContextualObject.getTitle())
                .setContentText(mContext.getString(R.string.notification_basic_description))
                .setContentIntent(viewPendingIntent)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setColor(mContext.getResources().getColor(R.color.primary))
                .setGroup(mGroupKey);

        return builder;
    }

    public Notification buildSummary() {
        NotificationCompat.Builder builder = buildBaseNotification();
        builder.setPriority(Notification.PRIORITY_HIGH);
        // Ugly hack, see http://stackoverflow.com/questions/26451893/heads-up-notification-android-lollipop
        builder.setVibrate(new long[0]);

        // Only display this one on the handheld device
        builder.setGroupSummary(true);

        return builder.build();
    }

    public Notification buildWearPlaceholder() {
        NotificationCompat.Builder builder = buildBaseNotification();
        builder.setGroupSummary(false);
        builder.setContentText(mContext.getString(R.string.context_loading));
        return builder.build();
    }

    /**
     * Builds the notification for the wear. <strong>This  method shouldn't be called from the main thread</strong>
     *
     * @return A Context Notification
     */
    public Notification buildWearNotification() {
        List<Notification> subPages;
        try {
            subPages = buildPages();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        NotificationCompat.Builder builder = buildBaseNotification();

        builder.setContentText(subPages.size() == 0 ? mContext.getString(R.string.context_has_no_match) : mContext.getString(R.string.context_has_match));
        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .addPages(subPages);

        return extender.extend(builder).build();

    }

    /**
     * Builds the sub-stacked notifications. <strong>This  method shouldn't be called from the main thread</strong>
     *
     * @return A List of Context Notification
     */
    public List<Notification> buildSubcontextWearNotifications() {
        List<Notification> notifs = new ArrayList<Notification>();
        List<ContextualObject> subContexts = mContextualObject.getSubContexts(mTailedEmails);
        if (subContexts != null) {
            for (ContextualObject subContext : subContexts) {
                notifs.add(new ContextNotificationBuilder(mContext)
                        .setContextualObject(subContext)
                        .setGroupKey(mGroupKey)
                        .buildWearNotification());
            }
        }
        return notifs;
    }

    /**
     * Add pages to the Wear extender
     *
     * @return
     * @throws Exception
     */
    private List<Notification> buildPages() throws Exception {
        DocumentsList documents = getContextualObjectDocuments();

        List<Notification> pages = new ArrayList<Notification>();
        for (int i = 0; i < documents.size() && i < WEAR_CONTEXT_SIZE; i++) {
            Document document = documents.get(i);
            String title = HtmlUtils.convertHlt(document.getTitle());

            // To build the big text, we retrieve the raw text
            // and then remove the "title" that will be set at the notification mainContent
            String bigText = AnyfetchHtmlUtils.htmlToSimpleHtmlTitleless(document.getSnippet(), document.getTitle());

            NotificationCompat.BigTextStyle bigView = new NotificationCompat.BigTextStyle();

            bigView.bigText(Html.fromHtml(bigText));

            // Standard View
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(mContext)
                            .setContentTitle(Html.fromHtml(title))
                            .setStyle(bigView)
                            .setSmallIcon(ImageHelper.matchResourceForProvider(document.getProviderId()));
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                    .setContentIcon(ImageHelper.matchResourceForProvider(document.getProviderId()));

            pages.add(extender.extend(builder).build());
        }
        return pages;
    }

    public DocumentsList getContextualObjectDocuments() throws Exception {
        if (mContextualObjectDocuments != null) {
            return mContextualObjectDocuments;
        }

        GetDocumentsListRequest request = (GetDocumentsListRequest) new DocumentsListRequestBuilder(mContext, WEAR_CONTEXT_SIZE)
                .setContextualObject(mContextualObject)
                .build();
        request.setOkHttpClient(mClient);
        mContextualObjectDocuments = request.loadDataFromNetwork();

        return mContextualObjectDocuments;
    }
}
