package com.anyfetch.companion.commons.models.demo;

import android.media.Image;

import com.anyfetch.companion.commons.R;
import com.anyfetch.companion.commons.models.Document;

import java.net.URL;

/**
 * A sample evernote note !
 */
public class DemoNote implements Document {
    @Override
    public String getTitle() {
        return "DF '14 Aftermeeting";
    }

    @Override
    public String getSnippet() {
        return "Debrief on the wearables presentation with anyfetch and salesforce";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_evernote;
    }

    @Override
    public URL getMobileURL() {
        return null;
    }
}