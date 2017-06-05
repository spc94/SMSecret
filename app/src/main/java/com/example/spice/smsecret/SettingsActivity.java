package com.example.spice.smsecret;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.example.spice.smsecret.Adapters.WhitelistAdapter;
import com.example.spice.smsecret.DAL.WhitelistedNumbersDAL;
import com.example.spice.smsecret.Model.WhitelistedNumber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SettingsActivity extends Activity {

    public String name = "";
    public String id;
    public String phone;
    public ArrayList<String> array = new ArrayList<>();
    public ListView listview;
    public WhitelistAdapter whitelistAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        array.clear();
        List<String> list = WhitelistedNumbersDAL.getAllWhitelistedNumbers();
        for(int i=0; i<list.size();i++){
            array.add(list.get(i));
        }

        whitelistAdapter= new WhitelistAdapter(this,array);
        listview.setAdapter(whitelistAdapter);
        whitelistAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final EditText etAddPhoneNumber = (EditText) findViewById(R.id.etAddPhoneNumber);
        Button btContactFromPB = (Button) findViewById(R.id.btContactFromPB);
        final Button btConfirm = (Button) findViewById(R.id.btConfirmText);
        listview = (ListView) findViewById(R.id.lvWhitelist);


        etAddPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()==0)
                    btConfirm.setVisibility(View.INVISIBLE);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btConfirm.setVisibility(View.VISIBLE);

            }
        });

        btContactFromPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, 1);
            }
        });

        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = etAddPhoneNumber.getText().toString();
                phoneNumber = countryDialingCodeToZeroes(phoneNumber);
                if(!WhitelistedNumbersDAL.contactExistsInDB(phoneNumber)) {
                    WhitelistedNumbersDAL.saveContactToDB(phoneNumber);
                    array.add(phoneNumber);
                    whitelistAdapter.notifyDataSetChanged();
                }
                hideSoftKeyboard();
                etAddPhoneNumber.setText("");
            }
        });


        //Adds to the Array all whitelisted contacts in the DB
        /*final List<String> list = WhitelistedNumbersDAL.getAllWhitelistedNumbers();
        for(int i=0; i<list.size();i++){
            array.add(Long.parseLong(list.get(i)));
        }



*/
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = adapterView.getAdapter().getItem(i).toString();
                WhitelistedNumbersDAL.deleteFromDB(s);
                array.remove(getItemIDFromString(s));
                whitelistAdapter.notifyDataSetChanged();
                return false;
            }
        });


    }

    public String countryDialingCodeToZeroes(String number){

        number.replaceAll(" ","");

        if(number.charAt(0)=='+')
            return "00" + number.substring(1);
        else
            return number;
    }

    public int getItemIDFromString(String s){

        for(int i=0; i<array.size(); i++){
            if(array.get(i).equals(s))
                return i;
        }
        return -1;
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
            id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            Log.v("Contact", "ID: " + id.toString());
            name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Log.v("Contact", "Name: " + name.toString());

            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                while (pCur.moveToNext()) {
                    phone = pCur.getString
                            (pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.v("getting phone number", "Phone Number: " + phone);
                    phone = countryDialingCodeToZeroes(phone);
                    phone = phone.replaceAll(" ","");
                    if(!WhitelistedNumbersDAL.contactExistsInDB(phone)) {
                        Log.d("DEBUG DAL","Contact doesn't exist in DB");
                        Log.d("DEBUG-TIAGO","NUMERO SETTINGS: "+phone);
                        WhitelistedNumbersDAL.saveContactToDB(phone);
                        array.add(phone);
                        whitelistAdapter.notifyDataSetChanged();
                    }
                }
            }

        }

    }


    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}


