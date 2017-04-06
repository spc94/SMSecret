package com.example.spice.smsecret;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spice.smsecret.DAL.WhitelistedNumbersDAL;
import com.example.spice.smsecret.UselessClasses.SimpleSpanBuilder;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ComposeActivity extends AppCompatActivity {

    int sizeOfEditTextBefore = 0;

    boolean canListenInput = true;
    final Vector<Long> contactsToSend = new Vector<>();
    final SpannableStringBuilder ssb = new SpannableStringBuilder();
    Map<String, Long> namePhoneCorrespondence = new HashMap<>();
    EditText etContactsToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        final TextView tvCharacterCount = (TextView) findViewById(R.id.tvCharacterCount);
        final ImageButton btAddContact = (ImageButton) findViewById(R.id.btAddContact);
        final ImageButton btSendMessage = (ImageButton) findViewById(R.id.btSendMessage);
        final EditText etMessageContents = (EditText) findViewById(R.id.etMessageContents);
        etContactsToSend = (EditText) findViewById(R.id.etContactToSend);


        btSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageContents = etMessageContents.getText().toString();
                String destinations = "smsto:";
                for (int i = 0; i < contactsToSend.size(); i++) {
                    destinations = destinations + contactsToSend.elementAt(i).toString() + ";";
                }
                destinations = destinations.subSequence(0, destinations.length() - 1).toString();
                Log.d("DEBUG", "Destination String: " + destinations);

                sendSMS(messageContents);
            }
        });


        etMessageContents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int numCharacters = etMessageContents.getText().toString().length();
                int totalMessages = 1;
                for (int i = 2, c = 160; (numCharacters / (float) c) > 1.0; i++) {
                    c = 160 * i;
                    totalMessages = i;
                }
                if (totalMessages == 1)
                    tvCharacterCount.setText("" + numCharacters + "/160");
                else
                    tvCharacterCount.setText("" + numCharacters + "/160 (" + totalMessages + ")");
            }
        });


        btAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, 1);
            }
        });

        etContactsToSend.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (view instanceof EditText && b == false) {
                    canListenInput = false;
                    UnderlineSpan us;
                    Editable buffer = ((EditText) view).getText();
                    ((EditText) view).setText("");
                    Vector<Long> v = bufferToVector(buffer);
                    ssb.clearSpans();
                    ssb.clear();
                    contactsToSend.clear();

                    if(buffer.length() == 0) {
                        canListenInput = true;
                        return;
                    }


                    else {

                        for (int i = 0; i < v.size(); i++) {
                            us = new UnderlineSpan();
                            contactsToSend.add(v.get(i));
                            ssb.append(v.get(i).toString(), us, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ssb.append(" ");
                        }
                        etContactsToSend.setText(ssb);
                        repositionEditText();
                        canListenInput = true;
                    }
                }
            }
        });


        etContactsToSend.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                int keyPressed = keyEvent.getKeyCode();

                if (keyPressed == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    canListenInput = false;
                    Log.d("DEBUG-99", "Key Pressed DEL");
                    if (view instanceof EditText) {
                        Editable buffer = ((EditText) view).getText();

                        if (buffer.length() == 0) {
                            canListenInput = true;
                            return false;
                        }

                        if (buffer.toString().charAt(buffer.toString().length() - 1) == ' ') {
                            buffer.delete(buffer.length(), buffer.length());
                            Log.d("DEBUG-99", "REMOVED SPACE: " + buffer.toString());

                            canListenInput = true;
                            return false;
                        }
                        // If the cursor is at the end of a Span then remove the whole Span
                        int start = Selection.getSelectionStart(buffer);
                        int end = Selection.getSelectionEnd(buffer);

                        if (start == end) {
                            UnderlineSpan[] us = buffer.getSpans(start, end, UnderlineSpan.class);
                            if (us.length > 0) {
                                buffer.toString();
                                Log.d("CREATE", "LENGTH > 0");
                                Log.d("CREATE", "SPAN START: " + buffer.getSpanStart(us[0]));
                                Log.d("CREATE", "SPAN STOP: " + buffer.getSpanEnd(us[0]));
                                String contactBeingRemoved = buffer.subSequence
                                        (buffer.getSpanStart(us[0]), buffer.getSpanEnd(us[0])).toString();
                                try {
                                    ssb.delete(buffer.getSpanStart(us[0]) - 1, buffer.getSpanEnd(us[0]));
                                } catch (Exception e) {
                                    ssb.delete(buffer.getSpanStart(us[0]), buffer.getSpanEnd(us[0]));
                                }
                                Log.d("DEBUG", "Contact Being Removed: " + contactBeingRemoved);
                                buffer.replace(
                                        buffer.getSpanStart(us[0]),
                                        buffer.getSpanEnd(us[0]),
                                        ""
                                );
                                Log.d("DEBUG", "" + buffer.toString());
                                buffer.removeSpan(us[0]);
                                removeElementByString(contactsToSend, contactBeingRemoved);
                                //Log.d("DEBUG-99","Contact Vector: "+contactsToSend.elementAt(0));
                                Log.d("DEBUG-99", "REMOVED SPAN: " + buffer.toString());

                                canListenInput = true;
                                return true;
                            } else {
                                buffer.delete(buffer.length(), buffer.length());
                                Log.d("DEBUG-99", "REMOVED LETTER: " + buffer.toString());

                                canListenInput = true;
                                return false;
                            }
                        }
                    }
                }
                return false;

            }

        });



        etContactsToSend.addTextChangedListener (new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                sizeOfEditTextBefore = charSequence.length();

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Won't parse the spacebar if we are deleting characters
                if(sizeOfEditTextBefore > charSequence.length()) {
                    canListenInput = false;
                    return;
                }
                else if(sizeOfEditTextBefore < i2){
                    canListenInput = true;
                    return;
                }

                if (charSequence.length() == 0) {
                    return;
                }

                else{
                    underlineNumber(charSequence.toString());
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void repositionEditText(){
        if(etContactsToSend.getText().toString().charAt(0) == ' ')
            etContactsToSend.getText().delete(0,1);
    }

    public Vector<Long> bufferToVector (Editable buffer){
        String s = buffer.toString();
        String array [] = s.split(" ");
        Vector<Long> v = new Vector<>();

        for(int i=0; i<array.length; i++){
            try{
                v.add(Long.parseLong(array[i]));
            }catch (Exception e){
                return v;
            }
        }

        return v;
    }

    public void underlineNumber(String charSequence){
        String sequence = charSequence.toString();

        if(sequence.charAt(sequence.length()-1) == ' ' && canListenInput == true){

            if (canListenInput) {
                canListenInput = false;

                String etContents = charSequence.toString();
                String currentNumber = getLastNumber(charSequence);


                Log.d("CONTACTS", "Number: " + currentNumber);
                Log.d("CONTACTS", "Size: " + currentNumber.length());
                try {
                    contactsToSend.add(Long.parseLong(currentNumber));
                }catch (Exception e){
                    return;
                }
                //Underlines text by setting a span
                UnderlineSpan us = new UnderlineSpan();

                ssb.append(currentNumber, us, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.append(" ");
                etContactsToSend.setText(ssb);
                repositionEditText();


                //Places cursor at the end
                etContactsToSend.setSelection(etContactsToSend.length(), etContactsToSend.length());
                canListenInput = true;
            }
        }
    }

    public boolean removeElementByString(Vector<Long> contactsToSend, String s){
        boolean flag = false;
        for(int i = 0; i<contactsToSend.size(); i++){
            try {
                if (contactsToSend.elementAt(i).equals(Long.parseLong(s))) {
                    contactsToSend.remove(i);
                    flag = true;
                }
            }catch (NumberFormatException e){
                //It means it is a string and we must find what number it is supposed to be and delete it
                long phoneNumber = namePhoneCorrespondence.get(s);

                for(int j = 0; j<contactsToSend.size(); j++){
                    if (contactsToSend.elementAt(i).equals(phoneNumber)) {
                        contactsToSend.remove(i);
                        namePhoneCorrespondence.remove(s);
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }


    public String getLastNumber(CharSequence text){
        char c = 'a';
        String number = "";

        if(text.length() == 0)
            return null;

        for(int i = text.length()-2; i != -1 && Character.isDigit(text.charAt(i));i--){
            c = text.charAt(i);
            number = number + c;
        }
        number = new StringBuilder(number).reverse().toString();
        return number;
    }

    public CharSequence getSpannable(String sNumber){
        SpannableString spannable = new SpannableString(sNumber);
        int length = spannable.length();
        if (length > 0) {
            spannable.setSpan(
                    new StringSpan(sNumber),
                    0,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return spannable;
    }

    public class StringSpan extends ClickableSpan {
        private final String string;

        public StringSpan(String string) {
            super();
            this.string = string;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(true);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            getContactData(data);
        }
    }

    public void getContactData(Intent data) {

        ContentResolver cr = getContentResolver();

        Uri contactData = data.getData();
        Log.v("Contact", contactData.toString());
        Cursor c = managedQuery(contactData, null, null, null, null);

        if (c.moveToFirst()) {

            String phone = "";
            String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            Log.v("Contact", "ID: " + id.toString());
            String contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Log.v("Contact", "Name: " + contactName.toString());

            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                while (pCur.moveToNext()) {
                    phone = pCur.getString
                            (pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.v("getting phone number", "Phone Number: " + phone);
                    //Removes eventual spaces from phone numbers
                    phone = phone.replaceAll(" ","");
                    contactsToSend.add(Long.parseLong(phone));

                    //Dictionary of Contact Name to Phone Number
                    namePhoneCorrespondence.put(contactName,Long.parseLong(phone));

                    //Underlines Contact Number and adds it to the EditText
                    UnderlineSpan us = new UnderlineSpan();
                    ssb.append(contactName, us, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.append(" ");
                    etContactsToSend.setText(ssb);
                    etContactsToSend.setSelection(etContactsToSend.length(), etContactsToSend.length());
                }
            }

        }

    }

    void sendSMS(String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        for(int i=0; i < contactsToSend.size(); i++)
            sms.sendTextMessage(contactsToSend.elementAt(i).toString(), null, message, sentPI, deliveredPI);



    }





;}
