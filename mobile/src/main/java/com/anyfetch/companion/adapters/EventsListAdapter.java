package com.anyfetch.companion.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyfetch.companion.R;
import com.anyfetch.companion.commons.android.pojo.Event;
import com.anyfetch.companion.commons.android.pojo.EventsList;
import com.anyfetch.companion.commons.android.pojo.Person;
import com.anyfetch.companion.ui.ImageHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Adapt events to a list
 */
public class EventsListAdapter extends TimedListAdapter implements StickyListHeadersAdapter {

    private final Context mContext;
    private final EventsList mEvents;

    /**
     * Creates a new events adapter
     *
     * @param context The app context
     * @param events  The events to show
     */
    public EventsListAdapter(Context context, EventsList events) {
        super(context);
        mContext = context;
        mEvents = events;
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return mEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.row_event, parent, false);
        }

        Event event = mEvents.get(position);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        imageView.setImageBitmap(createAttendeesMosaic(event.getAttendees()));

        TextView titleView = (TextView) convertView.findViewById(R.id.titleView);
        titleView.setText(event.getTitle());

        TextView locationView = (TextView) convertView.findViewById(R.id.locationView);
        String location = event.getLocation();
        if (location == null) {
            location = "";
        }
        locationView.setText(location);

        TextView timeView = (TextView) convertView.findViewById(R.id.timeView);
        Calendar start = Calendar.getInstance();
        start.setTime(event.getStartDate());
        Calendar end = Calendar.getInstance();
        end.setTime(event.getEndDate());

        if (end.getTimeInMillis() - start.getTimeInMillis() != 1000 * 60 * 60 * 24) {
            timeView.setText(
                    String.format("%02d:%02d - %02d:%02d",
                            start.get(Calendar.HOUR_OF_DAY),
                            start.get(Calendar.MINUTE),
                            end.get(Calendar.HOUR_OF_DAY),
                            end.get(Calendar.MINUTE)));
        } else {
            timeView.setText("");
        }

        TextView attendeeView = (TextView) convertView.findViewById(R.id.attendeeView);
        int attendees = event.getAttendees().size();
        if (attendees == 1) {
            attendeeView.setText(event.getAttendees().get(0).getName());
        } else {
            attendeeView.setText(String.format(mContext.getString(R.string.multiple_attendees), attendees));
        }

        return convertView;
    }

    @Override
    public Date getDate(int i) {
        return mEvents.get(i).getStartDate();
    }

    private Bitmap createAttendeesMosaic(List<Person> attendees) {
        List<Bitmap> thumbs = new ArrayList<Bitmap>();
        for (Person attendee : attendees) {
            Bitmap thumb = attendee.getThumb();
            if (thumb != null) {
                thumbs.add(thumb);
            }
        }
        int size = thumbs.size();
        if (size > 0) {
            return ImageHelper.getRoundedCornerBitmap(thumbs.get(0), 200);
        }

        // TODO: Change this icon
        return BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.ic_menu_today);
    }

    public Event getEvent(int position) {
        return mEvents.get(position);
    }
}
