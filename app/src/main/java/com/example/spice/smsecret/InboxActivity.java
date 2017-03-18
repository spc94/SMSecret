package com.example.spice.smsecret;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity implements View.OnClickListener{
    DatabaseHandler db = new DatabaseHandler(this);
    public int sizeOfTextViewArray;
    public static InboxActivity ins;
    public LinearLayout contactsLayout;
    public TextView[] textView;
    public ArrayList<Integer> contactsInView = new ArrayList<>();
    public int dbSize;

    public TextView tvWelcome;

    public static InboxActivity getInstance(){
        return ins;
    }

    @Override
    protected void onResume(){
        super.onResume();
        ins = this;
        contactsLayout = (LinearLayout) findViewById(R.id.contacts);
        cleanLayout();
        Log.d("DEBUG XXX", "Before Populating TV");
        textView = populateTextViewArray();
        Log.d("DEBUG XXX", "Before Adding Contacts to Layout");
        addContactsToLayout(contactsLayout, textView, sizeOfTextViewArray);
        Log.d("DEBUG XXX", "Before Init Listeners");
        initTextViewListeners();
        if(sizeOfTextViewArray == 0){
            tvWelcome.setVisibility(View.VISIBLE);
        }
        else{
            tvWelcome.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onClick(View arg0) {
        Log.d("CLICK","Clicked a contact number! ");
        TextView clickedView = (TextView)arg0;
        Log.d("DEBUG","id: "+ clickedView.getId());
        int id = contactsInView.get(clickedView.getId());
        Log.d("DEB","ID: "+id);
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra("contact",id);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        tvWelcome = (TextView) findViewById(R.id.tvInboxWelcome);
        ins = this;
        contactsLayout = (LinearLayout) findViewById(R.id.contacts);
        cleanLayout();
        textView = populateTextViewArray();
        addContactsToLayout(contactsLayout, textView, sizeOfTextViewArray);
        initTextViewListeners();
    }

    public TextView[] populateTextViewArray(){
        int size = db.getMessagesCount();
        dbSize = size;
        Log.d("DEB","Size of DB "+size);
        //Note - The number of textviews should depend on amount of different contacts and not messages
        final TextView[] myTextViews  = new TextView[size];
        contactsInView.clear();

        //for (int i = 1, j = 0; i < size+1; i++) {
        for (int i=size,j = 0; i>=1;i-- )  {
            int contactNumber = db.getMessage(i).getContactNumber();
            if (checkContactExists(contactNumber) == false) {
                contactsInView.add(contactNumber);
                String contactInPhonebook = checkWhitelistedContactExistsOnPhonebook(contactNumber);
                Log.d("DEBUG X", "contactInPhonebook = "+contactInPhonebook);

                //If contact doesn't exist in phonebook we use number
                if(contactInPhonebook == null) {
                    final TextView rowTextView = new TextView(this);
                    rowTextView.setText(String.valueOf(contactNumber));
                    rowTextView.setId(j);
                    myTextViews[j] = rowTextView;
                    Log.d("DEBUG X","CONTACT IS NULL");
                }
                // If contact exists in phonebook we use name instead
                else{
                    final TextView rowTextView = new TextView(this);
                    rowTextView.setText(contactInPhonebook);
                    rowTextView.setId(j);
                    myTextViews[j] = rowTextView;
                    Log.d("DEBUG X","CONTACT IS "+contactInPhonebook);
                }
                Log.d("DEBUG X","Content of new TextView = "+myTextViews[j].getText().toString());
                //Only increments array if we are not repeating contact
                j++;
                sizeOfTextViewArray = j;

            }
            else
                continue;

        }
        //Log.d("DEB","SIZE OF ARRAY = "+sizeOfTextViewArray);
        //Log.d("DEB","Contents of last position ="+myTextViews[sizeOfTextViewArray].getText().toString());
        Log.d("DEBUG 69","Size Of DB: "+size);
        Log.d("DEBUG 69","Size Of Textview Array: "+sizeOfTextViewArray);
        return myTextViews;

    }

    public String checkWhitelistedContactExistsOnPhonebook(int number) {
        ArrayList<String[]> contactsInPhonebook = getAllContactsFromPhonebook();
        for (int i = 0; i < contactsInPhonebook.size(); i++) {
            if (number == Integer.parseInt(contactsInPhonebook.get(i)[1]))
                return contactsInPhonebook.get(i)[0];
        }
        return null;
    }

    public ArrayList<String[]> getAllContactsFromPhonebook(){
        ArrayList<String[]> contacts = new ArrayList<>();

        Cursor managedCursor = getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]
                                {
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                                }, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        Log.d("DEBUG CONTACTS", "SIZE OF PHONEBOOK: "+managedCursor.getColumnCount());

        while(managedCursor.moveToNext()){
            String numberWithoutSpaces = managedCursor.getString(1).replaceAll(" ","");
            String [] temp = {managedCursor.getString(0),numberWithoutSpaces};
            contacts.add(temp);
            Log.d("DEBUG CONTACTS",""+managedCursor.getString(0));
            Log.d("DEBUG CONTACTS",""+managedCursor.getString(1));
        }

        return contacts;
    }

    public boolean checkContactExists(int contactNumber){

        for(int i=0; i<contactsInView.size();i++) {
            if (contactNumber == contactsInView.get(i))
                return true;
        }

        return false;
    }

    public void addContactsToLayout(LinearLayout contactsLayout, TextView[] tv, int size){
        //int flag = 0;

        if(dbSize ==0)
            return;

        for (int i = 0; i < size; i++) {
                Log.d("DEBUG XX","Contents of TextView before layout = "+tv[0].getText().toString());
                tv[i].setTextSize(25);
                if(!checkVisited(contactsInView.get(i)))
                    tv[i].setBackgroundColor(Color.rgb(0,160,0));
                else
                    tv[i].setBackgroundColor(Color.GREEN);
                contactsLayout.addView(tv[i]);
        }
    }


    public boolean checkVisited(int contactNumber){
        boolean checkVisited = db.checkVisited(contactNumber);
        Log.d("DEBUG","VISITED BEFORE: "+checkVisited);
        return checkVisited;
    }

    public void initTextViewListeners(){

        if(dbSize == 0)
            return;

        for (int i = 0; i < sizeOfTextViewArray; i++) {
            textView[i].setOnClickListener(this);
        }
    }

    public void cleanLayout(){
        contactsLayout.removeAllViews();
    }
}
