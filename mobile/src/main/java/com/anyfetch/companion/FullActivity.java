package com.anyfetch.companion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.anyfetch.companion.commons.api.builders.ContextualObject;
import com.anyfetch.companion.commons.api.pojo.Document;
import com.anyfetch.companion.fragments.FullFragment;

/**
 * Launches FullFragment
 */
public class FullActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);
        Intent originIntent = getIntent();

        Document document = originIntent.getParcelableExtra(FullFragment.ARG_DOCUMENT);
        ContextualObject contextualObject = originIntent.getParcelableExtra(FullFragment.ARG_CONTEXTUAL_OBJECT);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, FullFragment.newInstance(document, contextualObject))
                    .commit();
        }
    }
}
