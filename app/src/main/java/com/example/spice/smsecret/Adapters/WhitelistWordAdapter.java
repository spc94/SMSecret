package com.example.spice.smsecret.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.spice.smsecret.R;

import java.util.ArrayList;

/**
 * Created by spice on 6/30/17.
 */

public class WhitelistWordAdapter extends ArrayAdapter<String>{
    public ArrayList<String> words;

    public WhitelistWordAdapter(Context context, ArrayList<String> words) {
        super(context, 0, words);
        this.words = words;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lvitemtv, parent, false);
        }
        // Lookup view for data population
        //TextView tvIndex = (TextView) convertView.findViewById(R.id.lvitemtv_index);
        final TextView tvNumber = (TextView) convertView.findViewById(R.id.lvitemtv_number);
        // Populate the data into the template view using the data object
        String integer = getItem(position);
        //tvIndex.setText(""+position);
        tvNumber.setText(""+integer);

        tvNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d("DEBUG CLICK","LONG CLICK DETECTED");

                return false;
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }
}
