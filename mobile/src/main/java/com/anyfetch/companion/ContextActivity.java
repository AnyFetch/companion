package com.anyfetch.companion;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;

import com.anyfetch.companion.commons.android.pojo.Event;
import com.anyfetch.companion.commons.api.builders.ContextualObject;
import com.anyfetch.companion.commons.notifications.MeetingPreparationAlarm;
import com.anyfetch.companion.fragments.ContextFragment;

/**
 * Launches ContextFragment
 */
public class ContextActivity extends Activity {
    private ContextualObject mContextualObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context);
        Intent originIntent = getIntent();

        mContextualObject = (ContextualObject) originIntent.getParcelableExtra(ContextFragment.ARG_CONTEXTUAL_OBJECT);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ContextFragment.newInstance((Parcelable) mContextualObject))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.context, menu);
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(false);
            bar.setTitle(mContextualObject.getTitle());
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_prepare_on_wear:
                if (mContextualObject instanceof Event) {
                    Intent i = new Intent();
                    i.setAction("com.anyfetch.companion.SHOW_NOTIFICATION");
                    i.putExtra(MeetingPreparationAlarm.ARG_EVENT, (Event) mContextualObject);
                    this.sendBroadcast(i);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
