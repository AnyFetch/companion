package com.anyfetch.companion.commons.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Contact/Attendee
 */
public class Person implements Parcelable {
    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {

        @Override
        public Person createFromParcel(Parcel source) {
            long id = source.readLong();
            String name = source.readString();
            String company = source.readString();
            String job = source.readString();
            List<String> emails = new ArrayList<String>();
            source.readStringList(emails);
            List<String> numbers = new ArrayList<String>();
            source.readStringList(numbers);
            Bitmap thumb = source.readParcelable(ClassLoader.getSystemClassLoader());
            long imgId = source.readLong();
            return new Person(id, name, company, job, emails, numbers, thumb, imgId);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
    private static final String[] NAME_PROJECTION = new String[]{
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
    };
    private static final int PRJ_CON_ID = 0;
    private static final int PRJ_DISP_NAME = 1;
    private static final String[] PHOTO_PROJECTION = new String[]{
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Photo.PHOTO,
            ContactsContract.CommonDataKinds.Photo.PHOTO_ID
    };
    private static final int PRJ_PHOTO_THUMB = 1;
    private static final int PRJ_PHOTO_ID = 2;
    private static final String[] PHONE_PROJECTION = new String[]{
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.ADDRESS
    };
    private static final int PRJ_EMAIL_ADDRESS = 1;
    private static final String[] EMAIL_PROJECTION = new String[]{
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private static final int PRJ_PHONE_NUMBER = 1;
    private static final String[] JOB_PROJECTION = new String[]{
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Organization.COMPANY,
            ContactsContract.CommonDataKinds.Organization.TITLE,
    };
    private static final int PRJ_JOB_COMPANY = 1;
    private static final int PRJ_JOB_TITLE = 2;
    private static final int PERSON_PARCELABLE = 11;
    private final String mName;
    private final String mJob;
    private final String mCompany;
    private final List<String> mEmails;
    private final List<String> mNumbers;
    private final Bitmap mThumb;
    private final long mPhotoId;
    private final long mId;

    /**
     * Creates a new Person
     *
     * @param id      Their id
     * @param name    Their name
     * @param job     Their job name
     * @param company Their company
     * @param emails  Their emails
     * @param numbers Their phone numbers
     * @param thumb   Their photo thumb
     * @param photoId A reference to the photo
     */
    public Person(long id, String name, String company, String job, List<String> emails, List<String> numbers, Bitmap thumb, long photoId) {
        mId = id;
        mName = name;
        mCompany = company;
        mJob = job;
        mEmails = emails;
        mNumbers = numbers;
        mThumb = thumb;
        mPhotoId = photoId;
    }

    /**
     * Retrieve a person from their id
     *
     * @param context The context to fetch from
     * @param id      Their id
     * @return The person
     */
    public static Person getPerson(Context context, long id) {
        // This is an awful piece of code, if there's a way to do it better, well go on ! This is open to refactoring.
        ContentResolver cr = context.getContentResolver();

        Cursor nameCur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                NAME_PROJECTION,
                ContactsContract.Data.CONTACT_ID + "=" + id +
                        " and " +
                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        nameCur.moveToFirst();
        String name;
        if (nameCur.getCount() > 0) {
            name = nameCur.getString(PRJ_DISP_NAME);
        } else {
            return null;
        }
        nameCur.close();

        Cursor photoCur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                PHOTO_PROJECTION,
                ContactsContract.Data.CONTACT_ID + "=" + id +
                        " and " +
                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        photoCur.moveToFirst();
        Bitmap thumb = null;
        long photoId = 0;
        if (photoCur.getCount() > 0) {
            byte[] thumbBlob = photoCur.getBlob(PRJ_PHOTO_THUMB);
            if (thumbBlob != null) {
                thumb = BitmapFactory.decodeByteArray(thumbBlob, 0, thumbBlob.length);
                photoId = photoCur.getLong(PRJ_PHOTO_ID);
            }
        }
        photoCur.close();

        Cursor emCur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                EMAIL_PROJECTION,
                ContactsContract.Data.CONTACT_ID + "=" + id +
                        " and " +
                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        emCur.moveToFirst();
        List<String> emails = new ArrayList<String>();
        for (int i = 0; i < emCur.getCount(); i++) {
            emails.add(emCur.getString(PRJ_EMAIL_ADDRESS));
            emCur.moveToNext();
        }
        emCur.close();

        Cursor phCur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                PHONE_PROJECTION,
                ContactsContract.Data.CONTACT_ID + "=" + id +
                        " and " +
                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        phCur.moveToFirst();
        List<String> numbers = new ArrayList<String>();
        for (int i = 0; i < phCur.getCount(); i++) {
            numbers.add(phCur.getString(PRJ_PHONE_NUMBER));
            phCur.moveToNext();
        }
        phCur.close();

        String title = "";
        String company = "";
        Cursor jobCur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                JOB_PROJECTION,
                ContactsContract.Data.CONTACT_ID + "=" + id +
                        " and " +
                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        jobCur.moveToFirst();
        if (jobCur.getCount() > 0) {
            company = jobCur.getString(PRJ_JOB_COMPANY);
            title = jobCur.getString(PRJ_JOB_TITLE);
        }
        jobCur.close();

        return new Person(
                id,
                name,
                company,
                title,
                emails,
                numbers,
                thumb,
                photoId
        );
    }

    /**
     * Retrieve a person from one of their email address
     *
     * @param context The context to fetch from
     * @param email   Their email
     * @return The person
     */
    public static Person getPerson(Context context, String email) {
        ContentResolver cr = context.getContentResolver();
        Cursor emCur = cr.query(
                ContactsContract.Data.CONTENT_URI,
                EMAIL_PROJECTION,
                ContactsContract.CommonDataKinds.Email.ADDRESS + "='" + email + "'",
                null,
                null);
        emCur.moveToFirst();
        if (emCur.getCount() < 1) {
            return null;
        }
        long id = emCur.getLong(PRJ_CON_ID);
        emCur.close();
        return getPerson(context, id);
    }

    /**
     * Gets the id
     *
     * @return An ID
     */
    public long getId() {
        return mId;
    }

    /**
     * Gets the name
     *
     * @return A name
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the job
     *
     * @return A job title
     */
    public String getJob() {
        return mJob;
    }

    /**
     * Gets the company
     *
     * @return A company name
     */
    public String getCompany() {
        return mCompany;
    }

    /**
     * Gets the emails
     *
     * @return A list of emails
     */
    public List<String> getEmails() {
        return mEmails;
    }

    /**
     * Gets the phone numbers
     *
     * @return A list of numbers
     */
    public List<String> getNumbers() {
        return mNumbers;
    }

    /**
     * Gets the thumb
     *
     * @return A Bitmap
     */
    public Bitmap getThumb() {
        return mThumb;
    }

    /**
     * Gets the photo id
     *
     * @return An ID
     */
    public long getPhotoId() {
        return mPhotoId;
    }

    @Override
    public int describeContents() {
        return PERSON_PARCELABLE;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeString(mCompany);
        dest.writeString(mJob);
        dest.writeStringList(mEmails);
        dest.writeStringList(mNumbers);
        dest.writeParcelable(mThumb, flags);
        dest.writeLong(mPhotoId);
    }
}
