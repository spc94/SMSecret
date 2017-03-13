package com.example.spice.smsecret.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import com.example.spice.smsecret.*;
import com.example.spice.smsecret.DAL.WhitelistedNumbersDAL;

/**
 * Created by spice on 3/9/17..
 */

public class WhitelistAdapter extends ArrayAdapter<Long> {

    public ArrayList<Long> numbers;

    public WhitelistAdapter(Context context, ArrayList<Long> numbers) {
        super(context, 0, numbers);
        this.numbers = numbers;
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
        Long integer = getItem(position);
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