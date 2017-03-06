package com.example.spice.smsecret;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spice on 01/08/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "messageManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "messages";

    // Contacts Table Columns names
    private static final String KEY_ID = "msgId";
    private static final String KEY_NUMBER = "phoneNumber";
    private static final String KEY_MSG = "message";
    private static final String KEY_VISITED = "visited";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NUMBER + " INTEGER,"
                + KEY_MSG + " TEXT," + KEY_VISITED + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    public void addMessage(Contacts contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NUMBER, contact.getContactNumber()); // Contact Name
        values.put(KEY_MSG, contact.getMessages()); // Contact Phone Number
        values.put(KEY_VISITED,0);

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    public Contacts getMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] {KEY_ID,
                        KEY_NUMBER, KEY_MSG, KEY_VISITED}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();

        }
        Contacts contact = new Contacts(Integer.parseInt(cursor.getString(1)),
                cursor.getString(2), Integer.parseInt(cursor.getString(3)));

        return contact;
    }

    public void changeVisitedTrue(int contactNumber){
        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + TABLE_CONTACTS +
                             " SET " + KEY_VISITED + "='1' WHERE " + KEY_NUMBER + "=?";

        db.execSQL(updateQuery,new String[] {String.valueOf(contactNumber)});

    }

    public boolean checkVisited(int contactNumber){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + KEY_NUMBER + "=?";

        Cursor cursor = db.rawQuery(selectQuery,new String[] {String.valueOf(contactNumber)});

        boolean flag = true;

        if(cursor.moveToFirst()){
            do{
                Log.d("DEBUG","Value in Column: "+cursor.getString(3));
                if(cursor.getString(3).equals("0"))
                    flag = false;
            }while(cursor.moveToNext());
        }

        return flag;
    }


    public List<Contacts> getAllMessages() {
        List<Contacts> contactList = new ArrayList<Contacts>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contacts contact = new Contacts();
                contact.setContactNumber(Integer.parseInt(cursor.getString(1)));
                contact.setMessage(cursor.getString(2));
                contact.setVisited(Integer.parseInt(cursor.getString(3)));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public int getMessagesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        Log.d("DEB","Inside Message Count");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        Log.d("DEB", "Cursor value: "+cursor.getCount());
        return cursorCount;
    }

    public void deleteMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }



}
