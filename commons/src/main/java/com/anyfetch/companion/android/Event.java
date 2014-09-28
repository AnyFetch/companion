package com.anyfetch.companion.android;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Represents a calendar event
 */
public class Event {
    private static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
    };
    private static final int PRJ_EVT_ID = 0;
    private static final int PRJ_EVT_TITLE = 1;
    private static final int PRJ_EVT_DESCRIPTION= 2;
    private static final int PRJ_EVT_DTSTART = 3;
    private static final int PRJ_EVT_DTEND = 4;
    private static final int PRJ_EVT_LOC = 5;


    private static final String[] ATTENDEE_PROJECTION = new String[] {
            CalendarContract.Attendees.ATTENDEE_NAME,
            CalendarContract.Attendees.ATTENDEE_EMAIL,
    };
    private static final int PRJ_ATT_NAME = 0;
    private static final int PRJ_ATT_EMAIL = 1;

    private final String mId;
    private final String mTitle;
    private final String mDescription;
    private final Date mStartDate;
    private final Date mEndDate;
    private final List<Person> mAttendees;
    private final String mLocation;

    /**
     * Gets a certain amount of upcoming events
     * @param context A context to fetch the events and the amount from
     * @return
     */
    public static List<Event> getUpcomingEvents(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int amount = preferences.getInt("eventsAmount", 20);
        ContentResolver cr = context.getContentResolver();

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        Cursor evtCur = cr.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                "(" +
                        CalendarContract.Events.DTSTART + ">" + now.getTimeInMillis()
                        + " and " +
                        CalendarContract.Events._COUNT + "<" + amount +
                ")"
                ,
                null,
                CalendarContract.Events.DTSTART + " ASC");
        List<Event> events = new ArrayList<Event>();
        for (int i = 0; i < evtCur.getCount(); i++) {
            int eventId = evtCur.getInt(PRJ_EVT_ID);
            Cursor attCur = cr.query(
                    CalendarContract.Attendees.CONTENT_URI,
                    ATTENDEE_PROJECTION,
                    CalendarContract.Attendees.EVENT_ID + "=" + eventId,
                    null,
                    null
            );
            List<Person> attendees = new ArrayList<Person>();
            for (int j = 0; j < attCur.getCount(); j++) {
                List<String> emails = new ArrayList<String>();
                emails.add(attCur.getString(PRJ_ATT_EMAIL));
                attendees.add(new Person(
                        attCur.getString(PRJ_ATT_NAME),
                        emails,
                        null,
                        null,
                        null
                ));
            }
            Date dtStart = new Date(evtCur.getInt(PRJ_EVT_DTSTART));
            Date dtEnd = new Date(evtCur.getInt(PRJ_EVT_DTEND));
            events.add(new Event(
                    Integer.toString(eventId),
                    evtCur.getString(PRJ_EVT_TITLE),
                    evtCur.getString(PRJ_EVT_DESCRIPTION),
                    dtStart,
                    dtEnd,
                    attendees,
                    evtCur.getString(PRJ_EVT_LOC)
            ));
        }
        return events;
    }

    /**
     * Creates a new event
     * @param id The unique identifier for the event
     * @param title The title
     * @param description The description
     * @param startDate The beginning
     * @param endDate The end
     * @param attendees The attendees
     */
    public Event(String id, String title, String description, Date startDate, Date endDate, List<Person> attendees, String location) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mStartDate = startDate;
        mEndDate = endDate;
        mAttendees = attendees;
        mLocation = location;
    }

    /**
     * Gets the id
     * @return An unique identifier
     */
    public String getId() {
        return mId;
    }

    /**
     * Gets the title
     * @return A title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Gets the description
     * @return A description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Gets the beginning
     * @return A date
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Gets the end
     * @return A date
     */
    public Date getEndDate() {
        return mEndDate;
    }

    /**
     * Gets the attendees
     * @return A list of People
     */
    public List<Person> getAttendees() {
        return mAttendees;
    }

    /**
     * Gets the event location
     * @return A location string
     */
    public String getLocation() {
        return mLocation;
    }
}
