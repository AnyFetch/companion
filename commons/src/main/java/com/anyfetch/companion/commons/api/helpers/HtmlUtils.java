package com.anyfetch.companion.commons.api.helpers;

import android.content.Context;
import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Tools for helping with HTML-Related stuff
 */
public class HtmlUtils {
    public static final String DOCUMENT_PLACEHOLDER = "{{document}}";
    public static final String LOCALE_PLACEHOLDER = "{{locale}}";
    public static String baseDocumentHtml = null;

    /**
     * Will return true if the specified document require some JS to be formatted.
     *
     * @param document The document's string
     * @return Whether it will need JS or not
     */
    public static Boolean requireJavascript(String document) {
        return document.contains("anyfetch-date");
    }

    /**
     * Renders a document in it's proper context
     *
     * @param context  The android context
     * @param document The document to inject
     * @return A complete HTML page with CSS(+JS)
     */
    public static String renderDocument(Context context, String document) {
        if(baseDocumentHtml == null) {
            // Preload the HTML file from assets
            try {
                InputStream fin = context.getAssets().open("document.html");
                byte[] buffer = new byte[fin.available()];
                fin.read(buffer);
                fin.close();

                baseDocumentHtml = new String(buffer);
            } catch (IOException e) {
                Log.e("WTF", e.toString());
                baseDocumentHtml = DOCUMENT_PLACEHOLDER;
            }
        }

        String languageCode = Locale.getDefault().getLanguage();
        return baseDocumentHtml.replace(DOCUMENT_PLACEHOLDER, document).replace(LOCALE_PLACEHOLDER, languageCode);
    }

    /**
     * Convert highlights into simple bold text (for wear)
     *
     * @param origin The original highlighted text
     * @return The bolded text
     */
    public static String convertHlt(String origin) {
        return origin.replaceAll("<span[^>]+?anyfetch-hlt[^>]+?>(.+?)</span>", "<b>$1</b>");
    }

    /**
     * Selects a specific tag in the DOM
     *
     * @param origin The HTML to be selected from
     * @param tag The tag to select
     * @return The content of the tag
     */
    public static String selectTag(String origin, String tag) {
        InputStream is = new ByteArrayInputStream(origin.getBytes());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(is);
            return doc.getElementsByTagName(tag).item(0).getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Strip HTML tags from a string
     *
     * @param origin An HTML string
     * @return A text
     */
    public static String stripHtml(String origin) {
        return origin.replaceAll("</*[^>]+?/*>", "");
    }

}
