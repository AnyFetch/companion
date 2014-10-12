package com.anyfetch.companion;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.anyfetch.companion.commons.api.helpers.HtmlUtils;
import com.anyfetch.companion.commons.api.pojo.Document;
import com.anyfetch.companion.fragments.FullFragment;
import com.anyfetch.companion.ui.ImageHelper;

/**
 * Launches FullFragment
 */
public class FullActivity extends Activity {

    private Document mDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);
        Intent originIntent = getIntent();

        mDocument = originIntent.getParcelableExtra(FullFragment.ARG_DOCUMENT);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, FullFragment.newInstance(mDocument))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.full, menu);
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setIcon(ImageHelper.matchResourceForProvider(mDocument.getProvider()));
            bar.setTitle(HtmlUtils.stripHtml(mDocument.getTitle()));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
