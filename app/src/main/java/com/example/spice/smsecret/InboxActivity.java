package com.example.spice.smsecret;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InboxActivity extends AppCompatActivity implements View.OnClickListener{
    DatabaseHandler db = new DatabaseHandler(this);
    public int sizeOfTextViewArray;
    public static InboxActivity ins;
    public LinearLayout contactsLayout;
    public TextView[] textView;

    public static InboxActivity getInstance(){
        return ins;
    }

    @Override
    public void onClick(View arg0) {
        Log.d("CLICK","Ouchie in my mouth! ");
        TextView clickedView = (TextView)arg0;
        int id = Integer.parseInt(clickedView.getText().toString());
        Log.d("DEB","ID: "+id);
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra("contact",id);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        ins = this;
        contactsLayout = (LinearLayout) findViewById(R.id.contacts);
        textView = populateTextViewArray();
        addContactsToLayout(contactsLayout, textView, sizeOfTextViewArray);
        initTextViewListeners();
    }

    public TextView[] populateTextViewArray(){
        int size = db.getMessagesCount();
        Log.d("DEB","Size of DB "+size);
        final TextView[] myTextViews  = new TextView[size];

        for (int i = 1, j= 0; i < size+1; i++) {
            int contactNumber = db.getMessage(i).getContactNumber();
            if (checkContactExists(myTextViews, contactNumber,j) == false) {
                final TextView rowTextView = new TextView(this);
                rowTextView.setText(String.valueOf(contactNumber));
                //contactsLayout.addView(rowTextView);
                myTextViews[j] = rowTextView;

                //Only increments array if we are not repeating contact
                sizeOfTextViewArray = j;
                j++;

            }
            else
                continue;

        }
        Log.d("DEB","SIZE OF ARRAY = "+sizeOfTextViewArray);
        Log.d("DEB","Contents of last position ="+myTextViews[sizeOfTextViewArray].getText().toString());
        return myTextViews;

    }

    public boolean checkContactExists(TextView[] tv, int contactNumber, int currMaxSize){

        for(int i=0; i<currMaxSize;i++) {
            if (Integer.parseInt(tv[i].getText().toString()) == contactNumber)//getting text when there's nothing yet
                return true;
        }

        return false;
    }

    public void addContactsToLayout(LinearLayout contactsLayout, TextView[] tv, int size){
        for (int i = 0; i < size+1; i++) {
            contactsLayout.addView(tv[i]);
        }
    }

    public void initTextViewListeners(){
        for (int i = 0; i < sizeOfTextViewArray+1; i++) {
            textView[i].setOnClickListener(this);
        }
    }

    public void cleanLayout(){
        // contactsLayout.removeAllViews();
    }
}
