package com.example.spice.smsecret;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Vector;

/**
 * Created by spice on 02/08/16.
 */


public class UnencryptedMessages extends Activity {
    public LinearLayout messagesLayout;
    public String contactNumber;
    private static UnencryptedMessages ins;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_messages);
        messagesLayout = (LinearLayout)findViewById(R.id.messages);

        ImageButton replyButton = (ImageButton) findViewById(R.id.btReplyTo);
        //Obtain extra parameters
        Bundle b = getIntent().getExtras();
        //Assigned passed parameter to a var
        contactNumber = b.getString("contact");

        DatabaseHandler db = new DatabaseHandler(this);
        db.changeVisitedTrueUnencrypted(contactNumber);

        try {
            initMessages(String.valueOf(contactNumber));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Intent intent = new Intent(,ComposeActivity.class);
                //intent.putExtra("phoneNumber",contactNumber);
            }
        });
    }

    public void replyToOnClick(View v){
    }

    public void cleanLayout(){
        messagesLayout.removeAllViews();
    }

    public void initMessages(String contactNumber) throws GeneralSecurityException, IOException, ClassNotFoundException {
        //Cleans Layout
        cleanLayout();
        //Fills a Vector with all messages from specified contact
        Vector<String> messagesVector = getMessagesFromContact(contactNumber);
        //Copies messages from vector to array of text view
        TextView [] tvArray = populateTextViewArray(messagesVector);
        //Adds Messages to Layout
        addMessagesToLayout(tvArray);
    }

    public void addMessagesToLayout(TextView[] tvArray){
        for (int i = tvArray.length-1; i >= 0; i--) {
            String temp = tvArray[i].getText().toString();
            tvArray[i].setTextSize(24);
            tvArray[i].setTextColor(Color.WHITE);
            tvArray[i].setText(temp + "\n______________________________");
            messagesLayout.addView(tvArray[i]);
        }
    }

    public TextView[] populateTextViewArray(Vector<String> v){
        int size = v.size();
        final TextView[] tvArray = new TextView[size];

        for (int i = 0; i < size; i++) {
            final TextView t = new TextView(this);
            t.setText(v.elementAt(i));
            tvArray[i] = t;
        }
        return tvArray;
    }

    public Vector<String> getMessagesFromContact(String contact){
        Vector<String> v = new Vector<>();
        List<Contacts> list =  UnencryptedInbox.getInstance().db.getAllMessagesUnencrypted();
        for (int i = 0; i < list.size(); i++) {
            if(contact.equals(list.get(i).getContactNumber()))
                v.add(list.get(i).getMessages());
        }
        return v;
    }



    public static UnencryptedMessages getInstance(){
        return ins;
    }
}
