package com.anyfetch.companion.mobile.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyfetch.companion.android.Event;
import com.anyfetch.companion.android.EventsList;
import com.anyfetch.companion.android.Person;
import com.anyfetch.companion.mobile.R;
import com.anyfetch.companion.mobile.ui.ImageHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Adapt events to a list
 */
public class EventsListAdapter extends GroupedListAdapter<Event> {

    /**
     * Creates a new events adapter
     *
     * @param context The app context
     * @param events  The events to show
     */
    public EventsListAdapter(Context context, EventsList events) {
        super(context, R.layout.row_event, events);
    }

    @Override
    protected String getSection(Event element) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        Calendar then = Calendar.getInstance();
        then.setTime(element.getStartDate());
        if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
            return getContext().getString(R.string.date_today);
        } else if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) + 1 == then.get(Calendar.DAY_OF_YEAR)) {
            return getContext().getString(R.string.date_tomorrow);
        } else {
            return then.get(Calendar.DAY_OF_MONTH) + "/" + (then.get(Calendar.MONTH));
        }
    }

    @Override
    protected View getView(Event event, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_event, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        imageView.setImageBitmap(createAttendeesMosaic(event.getAttendees()));

        TextView titleView = (TextView) rowView.findViewById(R.id.titleView);
        titleView.setText(event.getTitle());

        TextView locationView = (TextView) rowView.findViewById(R.id.locationView);
        String location = event.getLocation();
        if(location == null) {
            location = "";
        }
        locationView.setText(location);

        TextView timeView = (TextView) rowView.findViewById(R.id.timeView);
        Calendar start = Calendar.getInstance();
        start.setTime(event.getStartDate());
        Calendar end = Calendar.getInstance();
        end.setTime(event.getEndDate());
        timeView.setText(
                String.format("%d:%d - %d:%d",
                        start.get(Calendar.HOUR_OF_DAY),
                        start.get(Calendar.MINUTE),
                        end.get(Calendar.HOUR_OF_DAY),
                        end.get(Calendar.MINUTE)));

        TextView attendeeView = (TextView) rowView.findViewById(R.id.attendeeView);
        int attendees = event.getAttendees().size();
        if (attendees > 0) {
            attendeeView.setText(String.format("%d %s", attendees, event.getAttendees().size() == 1 ? getContext().getString(R.string.one_attendee) : getContext().getString(R.string.multiple_attendees)));
        } else {
            attendeeView.setText("");
        }

        return rowView;
    }

    private Bitmap createAttendeesMosaic(List<Person> attendees) {
        List<Bitmap> thumbs = new ArrayList<Bitmap>();
        for(Person attendee : attendees) {
            Bitmap thumb = attendee.getThumb();
            if(thumb != null) {
                thumbs.add(thumb);
            }
        }
        int size = thumbs.size();
        if(size > 0) {
            return ImageHelper.getRoundedCornerBitmap(thumbs.get(0), 200);
        }

        // TODO: Change this icon
        return BitmapFactory.decodeResource(getContext().getResources(), android.R.drawable.ic_menu_today);
    }
}
