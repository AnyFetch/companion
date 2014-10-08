package com.anyfetch.companion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.anyfetch.companion.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Generates a ListAdapter and group items in sections
 */
public abstract class GroupedListAdapter<T> extends ArrayAdapter<T> {
    private final Context mContext;
    private HashMap<String, List<T>> mSections;
    private List<String> mSectionsOrder;

    /**
     * Creates a new GroupedListAdapter
     *
     * @param context  The app context
     * @param resource The base resource
     * @param elements The elements in the list
     */
    public GroupedListAdapter(Context context, int resource, List<T> elements) {
        super(context, resource);
        mContext = context;
        mSections = new HashMap<String, List<T>>();
        mSectionsOrder = new ArrayList<String>();
        for (T element : elements) {
            String sectionName = getSection(element);
            if (!mSections.containsKey(sectionName)) {
                mSections.put(sectionName, new ArrayList<T>());
                mSectionsOrder.add(sectionName);
            }
            mSections.get(sectionName).add(element);
        }
    }

    /**
     * Gets the section name for an element
     *
     * @param element The element
     * @return A title
     */
    protected abstract String getSection(T element);

    /**
     * Gets the row view for an element
     *
     * @param element     The element
     * @param convertView The convert view
     * @param parent      The parent view
     * @return A view (the row)
     */
    protected abstract View getView(T element, View convertView, ViewGroup parent);

    /**
     * Gets the element for the position in the list
     *
     * @param position The position given by the listener
     * @return The element matching this position
     */
    public T getElement(int position) {
        int counted = 0;
        for (String sectionName : mSectionsOrder) {
            List<T> elements = mSections.get(sectionName);
            if (counted == position) { // header
                return null;
            } else if (counted < position && position <= counted + elements.size()) { // element
                return elements.get(position - 1 - counted);
            }
            counted += 1 + elements.size();
        }
        return null;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (String sectionName : mSectionsOrder) {
            count++;
            count += mSections.get(sectionName).size();
        }
        return count;
    }

    @Override
    public boolean isEnabled(int position) {
        int counted = 0;
        for (String sectionName : mSectionsOrder) {
            List<T> elements = mSections.get(sectionName);
            if (counted == position) { // header
                return false;
            } else if (counted < position && position <= counted + elements.size()) { // element
                return true;
            }
            counted += 1 + elements.size();
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T element = getElementAtPosition(position);

        if (element == null) {
            // We're displaying a section
            //if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_group_header, parent, false);
            //}

            TextView titleView = (TextView) convertView.findViewById(R.id.titleView);
            titleView.setText(getSectionAtPosition(position));
            return convertView;
        } else {
            return getView(element, convertView, parent);
        }
    }

    public int getViewTypeCount() {
        return 2; // Standard view + grouped list view
	}

    public int getItemViewType(int position) {
        if (getElementAtPosition(position) == null) {
            return 0;
        } else {
            return 1;
        }
    }

    public T getElementAtPosition(int position) {
        T element = null;
        String section = "";

        int counted = 0;
        for (String sectionName : mSectionsOrder) {
            List<T> elements = mSections.get(sectionName);
            if (counted == position) { // header
                section = sectionName;
                break;
            } else if (counted < position && position <= counted + elements.size()) { // element
                element = elements.get(position - 1 - counted);
                break;
            }
            counted += 1 + elements.size();
        }

        return element;
    }


    /**
     * Returns the section at specified position. Works even if position holds an event, in which case the current section is returned
     *
     * @param position
     * @return section name
     */
    public String getSectionAtPosition(int position) {
        String section = "";

        int counted = 0;
        for (String sectionName : mSectionsOrder) {
            List<T> elements = mSections.get(sectionName);
            if (counted == position) { // header
                section = sectionName;
                break;
            } else if (counted < position && position <= counted + elements.size()) { // element
                break;
            }
            counted += 1 + elements.size();
        }

        return section;
    }
}
