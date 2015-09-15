package com.superiorinfotech.publicbuddy.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by alex on 28.01.15.
 */
public class SubcategoryArrayAdapter extends ArrayAdapter {

    public SubcategoryArrayAdapter(Context context, int resource) {
        super(context, resource);
    }
    public SubcategoryArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }
    public SubcategoryArrayAdapter(Context context, int resource, Object[] objects) {
        super(context, resource, objects);
    }
    public SubcategoryArrayAdapter(Context context, int resource, int textViewResourceId, Object[] objects) {
        super(context, resource, textViewResourceId, objects);
    }
    public SubcategoryArrayAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
    }
    public SubcategoryArrayAdapter(Context context, int resource, int textViewResourceId, List objects) {
        super(context, resource, textViewResourceId, objects);
    }
}
