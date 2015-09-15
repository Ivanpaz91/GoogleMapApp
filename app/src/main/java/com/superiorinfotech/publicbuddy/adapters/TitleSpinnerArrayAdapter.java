package com.superiorinfotech.publicbuddy.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.superiorinfotech.publicbuddy.R;

import java.util.List;

/**
 * Created by alex on 15.01.15.
 */
public class TitleSpinnerArrayAdapter extends ArrayAdapter {
    private String caption = "";


    public TitleSpinnerArrayAdapter(Context context, int resource, int textViewResourceId, List objects, String caption) {
        super(context, resource, textViewResourceId, objects);

//        this.caption = caption+":";
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);

        ((TextView)view.findViewById(R.id.caption)).setText(caption);


        return view;
    }
}
