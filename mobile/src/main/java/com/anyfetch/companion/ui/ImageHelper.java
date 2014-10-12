package com.anyfetch.companion.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.anyfetch.companion.R;

/**
 * Various helpers to work with bitmaps
 */
public class ImageHelper {
    /**
     * Round the corners of an image
     *
     * @param bitmap The image
     * @param pixels The border radius
     * @return A new bitmap
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static int matchResourceForProvider(String provider) {
        if (provider.equals("Dropbox")) {
            return R.drawable.ic_dropbox;
        } else if (provider.equals("Evernote")) {
            return R.drawable.ic_evernote;
        } else if (provider.equals("GCalendar")) {
            return R.drawable.ic_gcalendar;
        } else if (provider.equals("Google Contacts")) {
            return R.drawable.ic_gcontacts;
        } else if (provider.equals("Google Drive")) {
            return R.drawable.ic_gdrive;
        } else if (provider.equals("GMail")) {
            return R.drawable.ic_gmail;
        } else if (provider.equals("SalesForce")) {
            return R.drawable.ic_sfdc;
        } else {
            return R.drawable.ic_launcher;
        }
    }
}