package com.anyfetch.companion.commons.android.pojo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

import com.anyfetch.companion.commons.api.builders.ContextualObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Represents a calendar event
 */
public class Event implements Parcelable, ContextualObject {
    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {

        @Override
        public Event createFromParcel(Parcel source) {
            long id = source.readLong();
            String title = source.readString();
            String description = source.readString();
            Date start = new Date(source.readLong());
            Date end = new Date(source.readLong());
            List<Person> attendees = new ArrayList<Person>();
            source.readTypedList(attendees, Person.CREATOR);
            String location = source.readString();

            return new Event(id, title, description, start, end, attendees, location);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    private static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
    };
    private static final int PRJ_EVT_ID = 0;
    private static final int PRJ_EVT_TITLE = 1;
    private static final int PRJ_EVT_DESCRIPTION = 2;
    private static final int PRJ_EVT_DTSTART = 3;
    private static final int PRJ_EVT_DTEND = 4;
    private static final int PRJ_EVT_LOC = 5;
    private static final String[] ATTENDEE_PROJECTION = new String[]{
            CalendarContract.Attendees.ATTENDEE_NAME,
            CalendarContract.Attendees.ATTENDEE_EMAIL,
    };
    private static final int PRJ_ATT_NAME = 0;
    private static final int PRJ_ATT_EMAIL = 1;
    private static final int EVENT_PARCELABLE = 10;
    private final long mId;
    private final String mTitle;
    private final String mDescription;
    private final Date mStartDate;
    private final Date mEndDate;
    private final List<Person> mAttendees;
    private final String mLocation;

    /**
     * Creates a new event
     *
     * @param id          The unique identifier for the event
     * @param title       The title
     * @param description The description
     * @param startDate   The beginning
     * @param endDate     The end
     * @param attendees   The attendees
     */
    public Event(long id, String title, String description, Date startDate, Date endDate, List<Person> attendees, String location) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mStartDate = startDate;
        mEndDate = endDate;
        mAttendees = attendees;
        mLocation = location;
    }

    private static Event fromCursor(Context context, Cursor cur) {
        ContentResolver cr = context.getContentResolver();
        String title = cur.getString(PRJ_EVT_TITLE);
        String description = cur.getString(PRJ_EVT_DESCRIPTION);
        Date dtStart = new Date(cur.getLong(PRJ_EVT_DTSTART));
        Date dtEnd = new Date(cur.getLong(PRJ_EVT_DTEND));
        String location = cur.getString(PRJ_EVT_LOC);
        int eventId = cur.getInt(PRJ_EVT_ID);
        Cursor attCur = cr.query(
                CalendarContract.Attendees.CONTENT_URI,
                ATTENDEE_PROJECTION,
                CalendarContract.Attendees.EVENT_ID + "=" + eventId,
                null,
                null
        );
        attCur.moveToFirst();
        List<Person> attendees = new ArrayList<Person>();
        for (int i = 0; i < attCur.getCount(); i++) {
            String email = attCur.getString(PRJ_ATT_EMAIL);
            Person attendee = Person.getPerson(context, email);
            if (attendee == null) {
                List<String> emails = new ArrayList<String>();
                emails.add(email);
                attendee = new Person(
                        0,
                        attCur.getString(PRJ_ATT_NAME),
                        "",
                        "",
                        emails,
                        new ArrayList<String>(),
                        null,
                        0
                );
            }
            attendees.add(attendee);
            attCur.moveToNext();
        }
        attCur.close();
        return new Event(
                eventId,
                title,
                description,
                dtStart,
                dtEnd,
                attendees,
                location
        );
    }

    /**
     * Gets a specific event
     *
     * @param context The android context to fetch from
     * @param id      The event id
     */
    public static Event getEvent(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor evtCur = cr.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                CalendarContract.Events._ID + "=" + id,
                null,
                null);
        evtCur.moveToFirst();
        if (evtCur.getCount() < 1) {
            return null;
        }
        Event event = fromCursor(context, evtCur);
        evtCur.close();
        return event;
    }

    /**
     * Gets the upcoming events
     *
     * @param context A context to fetch the events from
     * @return A list of events
     */
    public static EventsList getUpcomingEvents(Context context) {
        return getUpcomingEvents(context, 50);
    }

    /**
     * Gets the upcoming events
     *
     * @param context A context to fetch the events from
     * @param limit   The amount of maximum events to fetch
     * @return A list of events
     */
    public static EventsList getUpcomingEvents(Context context, int limit) {
        ContentResolver cr = context.getContentResolver();

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        Cursor evtCur = cr.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                CalendarContract.Events.DTEND + ">" + now.getTimeInMillis()
                ,
                null,
                CalendarContract.Events.DTSTART + " ASC LIMIT " + limit);
        EventsList events = new EventsList();
        evtCur.moveToFirst();
        for (int i = 0; i < evtCur.getCount(); i++) {
            events.add(fromCursor(context, evtCur));
            evtCur.moveToNext();
        }
        evtCur.close();
        return events;
    }

    /**
     * Gets the id
     *
     * @return An unique identifier
     */
    public long getId() {
        return mId;
    }

    /**
     * Gets the title
     *
     * @return A title
     */
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSearchQuery() {
        String query = "(" + mTitle + ")";
        for (Person attendee : mAttendees) {
            query += " OR " + attendee.getSearchQuery();
        }
        return query;
    }

    /**
     * Gets the description
     *
     * @return A description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Gets the beginning
     *
     * @return A date
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Gets the end
     *
     * @return A date
     */
    public Date getEndDate() {
        return mEndDate;
    }

    /**
     * Gets the attendees
     *
     * @return A list of People
     */
    public List<Person> getAttendees() {
        return mAttendees;
    }

    /**
     * Gets the event location
     *
     * @return A location string
     */
    public String getLocation() {
        return mLocation;
    }

    @Override
    public int describeContents() {
        return EVENT_PARCELABLE;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeLong(mStartDate.getTime());
        dest.writeLong(mEndDate.getTime());
        dest.writeTypedList(mAttendees);
        dest.writeString(mLocation);
    }
}
