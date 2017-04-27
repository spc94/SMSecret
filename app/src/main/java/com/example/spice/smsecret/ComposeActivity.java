package com.example.spice.smsecret;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.android.ex.chips.RecipientEditTextView;
import com.example.spice.smsecret.DAL.WhitelistedNumbersDAL;
import com.example.spice.smsecret.UselessClasses.SimpleSpanBuilder;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ComposeActivity extends AppCompatActivity {

    int sizeOfEditTextBefore = 0;
    private static final int SELECT_PHOTO = 100;
    boolean canListenInput = true;
    final Vector<String> contactsToSend = new Vector<>();
    final SpannableStringBuilder ssb = new SpannableStringBuilder();
    private Bitmap attachmentBitmap;
    boolean hasAttachment = false;
    Map<String, String> namePhoneCorrespondence = new HashMap<>();
    EditText etContactsToSend;
    RecipientEditTextView retContactsToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        final TextView tvCharacterCount = (TextView) findViewById(R.id.tvCharacterCount);
        final ImageButton btAddContact = (ImageButton) findViewById(R.id.btAddContact);
        final ImageButton btSendMessage = (ImageButton) findViewById(R.id.btSendMessage);
        final EditText etMessageContents = (EditText) findViewById(R.id.etMessageContents);
        final ImageButton btAttachment = (ImageButton) findViewById(R.id.btAttachment);



        etContactsToSend = (EditText) findViewById(R.id.etContactToSend);
        //retContactsToSend = (RecipientEditTextView) findViewById(R.id.retContacts);




        btAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        btSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*if (view instanceof EditText) {
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

                    finish();
                }*/

                String messageContents = etMessageContents.getText().toString();
                String destinations = "smsto:";
                for (int i = 0; i < contactsToSend.size(); i++) {
                    destinations = destinations + contactsToSend.elementAt(i).toString() + ";";
                }
                destinations = destinations.subSequence(0, destinations.length() - 1).toString();
                destinations = destinations.replaceAll(":;",":");
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
                    Vector<String> v = bufferToVector(buffer);
                    ssb.clearSpans();
                    ssb.clear();
                    contactsToSend.clear();

                    if(buffer.length() == 0) {
                        canListenInput = true;
                        return;
                    }


                    else {

                        String temp = "";

                        for (int i = 0; i < v.size(); i++) {
                            us = new UnderlineSpan();
                            temp = namePhoneCorrespondence.get(v.get(i));
                            if(temp != null)
                                contactsToSend.add(temp);
                            else
                                contactsToSend.add(v.get(i));

                            Log.d("DEB6","ContactKKK: "+v.get(i));
                            Log.d("DEB6","ContactKKK: "+namePhoneCorrespondence.get(v.get(i)));
                            ssb.append(v.get(i), us, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public Vector<String> bufferToVector (Editable buffer){

        String s = buffer.toString();
        String array [] = s.split(" ");
        Vector<String> v = new Vector<>();
        for(int i=0; i<array.length; i++){

            /*if(!array[i].matches(".*\\d+.*")) {
                v.add(namePhoneCorrespondence.get(array[i]));
                continue;
            }*/
            try{
                v.add(array[i]);
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
                    contactsToSend.add(currentNumber);
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

    public boolean removeElementByString(Vector<String> contactsToSend, String s){
        boolean flag = false;
        for(int i = 0; i<contactsToSend.size(); i++){
            try {
                if (contactsToSend.elementAt(i).equals(s)) {
                    contactsToSend.remove(i);
                    flag = true;
                }
            }catch (NumberFormatException e){
                //It means it is a string and we must find what number it is supposed to be and delete it
                String phoneNumber = namePhoneCorrespondence.get(s);

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

        if(requestCode == SELECT_PHOTO){
            if(resultCode == RESULT_OK){
                Uri selectedImage = data.getData();

                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this,"Image not found!",Toast.LENGTH_SHORT);
                }
                Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);

                if(yourSelectedImage.getByteCount()<307200) {
                    attachmentBitmap = yourSelectedImage;
                    hasAttachment = true;
                }
                else{
                    Toast.makeText(this,"The image is too large!",Toast.LENGTH_SHORT).show();
                }

            }
        }

        else if (resultCode == RESULT_OK) {
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

                if(pCur.moveToNext()){
                    phone = pCur.getString
                            (pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.v("getting phone number", "Phone Number: " + phone);
                    //Removes eventual spaces from phone numbers
                    phone = phone.replaceAll(" ","");
                    phone = countryDialingCodeToZeroes(phone);
                    Log.d("DEBUG","Phone without dialing code: "+phone);
                    contactsToSend.add(phone);

                    //Dictionary of Contact Name to Phone Number
                    namePhoneCorrespondence.put(contactName.replaceAll(" ",""),phone);

                    //Underlines Contact Number and adds it to the EditText
                    UnderlineSpan us = new UnderlineSpan();
                    ssb.append(contactName.replaceAll(" ",""), us, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssb.append(" ");
                    etContactsToSend.setText(ssb);
                    Log.d("DEBUG" ,"TEXT SET");
                    etContactsToSend.setSelection(etContactsToSend.length(), etContactsToSend.length());
                }
            }

        }

    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 150;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }

    public String countryDialingCodeToZeroes(String number){

        if(number.charAt(0)=='+')
            return "00" + number.substring(1);
        else
            return number;
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
        if(hasAttachment == false) {
            for (int i = 0; i < contactsToSend.size(); i++)
                sms.sendTextMessage(contactsToSend.elementAt(i).toString(), null, message, sentPI, deliveredPI);
        }
        else{
            Settings settings = new Settings();
            settings.setUseSystemSending(true);
            Transaction transaction = new Transaction(this, settings);
            Message multimediaMessage;
            Log.d("DEBUGIMAGE","IT ENTERS THE FUCKING ELSE");
            for(int i = 0; i < contactsToSend.size(); i++){
                multimediaMessage = new Message(message, contactsToSend.elementAt(i));
                multimediaMessage.setImage(attachmentBitmap);
                transaction.sendNewMessage(multimediaMessage, Transaction.NO_THREAD_ID);
            }
            Toast.makeText(this,"MMS successfully sent!", Toast.LENGTH_SHORT).show();
        }

    }





;}
