package com.example.spice.smsecret;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
    private static final String TABLE_CONTACTS_UNECNRYPTED = "messages_unencrypted";
    // Contacts Table Columns names
    private static final String KEY_ID = "msgId";
    private static final String KEY_NUMBER = "phoneNumber";
    private static final String KEY_MSG = "message";
    private static final String KEY_VISITED = "visited";
    private static final String KEY_JUNK_FLAG = "junk";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NUMBER + " TEXT,"
                + KEY_MSG + " TEXT," + KEY_VISITED + " INTEGER," + KEY_JUNK_FLAG + " INTEGER" + ")";

        String CREATE_CONTACTS_TABLE_UNENCRYPTED = "CREATE TABLE " + TABLE_CONTACTS_UNECNRYPTED + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NUMBER + " TEXT,"
                + KEY_MSG + " TEXT," + KEY_VISITED + " INTEGER," + KEY_JUNK_FLAG + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE_UNENCRYPTED);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_UNECNRYPTED);
        // Create tables again
        onCreate(db);
    }

    public void addMessage(Contacts contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NUMBER, contact.getContactNumber()); // Contact Name
        values.put(KEY_MSG, contact.getMessages()); // Contact Phone Number
        values.put(KEY_VISITED,0);
        values.put(KEY_JUNK_FLAG,contact.getJunkFlag());

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    public void addMessageUnencrypted(Contacts contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NUMBER, contact.getContactNumber()); // Contact Name
        values.put(KEY_MSG, contact.getMessages()); // Contact Phone Number
        values.put(KEY_VISITED,0);
        values.put(KEY_JUNK_FLAG,contact.getJunkFlag());

        // Inserting Row
        db.insert(TABLE_CONTACTS_UNECNRYPTED, null, values);
        db.close(); // Closing database connection
    }

    public Contacts getMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] {KEY_ID,
                        KEY_NUMBER, KEY_MSG, KEY_VISITED, KEY_JUNK_FLAG}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();

        }
        Contacts contact = new Contacts(cursor.getString(1),
                cursor.getString(2), Integer.parseInt(cursor.getString(3)),Integer.parseInt(cursor.getString(4)));

        return contact;
    }

    public Contacts getMessageUnencrypted(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS_UNECNRYPTED, new String[] {KEY_ID,
                        KEY_NUMBER, KEY_MSG, KEY_VISITED, KEY_JUNK_FLAG}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();

        }
        Contacts contact = new Contacts(cursor.getString(1),
                cursor.getString(2), Integer.parseInt(cursor.getString(3)),Integer.parseInt(cursor.getString(4)));

        return contact;
    }

    public void changeVisitedTrue(String contactNumber){
        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + TABLE_CONTACTS +
                             " SET " + KEY_VISITED + "='1' WHERE " + KEY_NUMBER + "=?";

        db.execSQL(updateQuery,new String[] {contactNumber});

    }

    public void changeVisitedTrueUnencrypted(String contactNumber){
        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + TABLE_CONTACTS_UNECNRYPTED +
                " SET " + KEY_VISITED + "='1' WHERE " + KEY_NUMBER + "=?";

        db.execSQL(updateQuery,new String[] {contactNumber});

    }

    public boolean checkVisited(String contactNumber){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + KEY_NUMBER + "=?";

        Cursor cursor = db.rawQuery(selectQuery,new String[] {contactNumber});

        boolean flag = true;

        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(3).equals("0"))
                    flag = false;
            }while(cursor.moveToNext());
        }

        return flag;
    }



    public boolean checkVisitedUnencrypted(String contactNumber){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS_UNECNRYPTED + " WHERE " + KEY_NUMBER + "=?";

        Cursor cursor = db.rawQuery(selectQuery,new String[] {contactNumber});

        boolean flag = true;

        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(3).equals("0"))
                    flag = false;
            }while(cursor.moveToNext());
        }

        return flag;
    }

    public int getNumberOfUnvisited(){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;

        Cursor cursor = db.rawQuery(selectQuery,null);

        int totalUnread = 0;

        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(3).equals("0"))
                    totalUnread += 1;
            }while(cursor.moveToNext());
        }

        return totalUnread;
    }

    public int getNumberOfUnvisitedUnencrypted(){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS_UNECNRYPTED;

        Cursor cursor = db.rawQuery(selectQuery,null);

        int totalUnread = 0;

        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(3).equals("0") && cursor.getString(4).equals("0"))
                    totalUnread += 1;
            }while(cursor.moveToNext());
        }

        return totalUnread;
    }

    public int getNumberOfUnvisitedJunk(){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS_UNECNRYPTED;

        Cursor cursor = db.rawQuery(selectQuery,null);

        int totalUnread = 0;

        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(3).equals("0") && cursor.getString(4).equals("1"))
                    totalUnread += 1;
            }while(cursor.moveToNext());
        }

        return totalUnread;
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
                contact.setContactNumber(cursor.getString(1));
                contact.setMessage(cursor.getString(2));
                contact.setVisited(Integer.parseInt(cursor.getString(3)));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public List<Contacts> getAllMessagesUnencrypted() {
        List<Contacts> contactList = new ArrayList<Contacts>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS_UNECNRYPTED;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contacts contact = new Contacts();
                contact.setContactNumber(cursor.getString(1));
                contact.setMessage(cursor.getString(2));
                contact.setVisited(Integer.parseInt(cursor.getString(3)));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public List<Contacts> getAllJunkMessages() {
        List<Contacts> contactList = new ArrayList<Contacts>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS_UNECNRYPTED;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            if(cursor.getInt(3)==1) {
                do {
                    Contacts contact = new Contacts();
                    contact.setContactNumber(cursor.getString(1));
                    contact.setMessage(cursor.getString(2));
                    contact.setVisited(Integer.parseInt(cursor.getString(3)));
                    // Adding contact to list
                    contactList.add(contact);
                } while (cursor.moveToNext());
            }
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

    public int getMessagesCountUnencrypted() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS_UNECNRYPTED;
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

    public void deleteMessageUnencrypted(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS_UNECNRYPTED, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteNTHRowUnencrypted(int id){
        int rowToDelete  = id;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+TABLE_CONTACTS_UNECNRYPTED+" where "+ KEY_ID+" = " +
                "(select "+KEY_ID+" from (select "+KEY_ID+" from "+TABLE_CONTACTS_UNECNRYPTED+" order by " +
                KEY_ID + " limit " + rowToDelete + ",1) as t)");
    }

    public void deleteNTHRowEncrypted(int id){
        int rowToDelete  = id;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+TABLE_CONTACTS+" where "+ KEY_ID+" = " +
                "(select "+KEY_ID+" from (select "+KEY_ID+" from "+TABLE_CONTACTS+" order by " +
                KEY_ID + " limit " + rowToDelete + ",1) as t)");
    }

    public int getMaxIDEncrypted(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_ID +" FROM " + TABLE_CONTACTS + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor != null) {
            cursor.moveToFirst();
        }
        try {
            int ret = cursor.getInt(0);
            return ret;
        }catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            return -1;
        }

    }

    public int getMaxIDUnencrypted(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_ID +" FROM " + TABLE_CONTACTS_UNECNRYPTED + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor != null) {
            cursor.moveToFirst();
        }
        try {
            int ret = cursor.getInt(0);
            return ret;
        }catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            return -1;
        }

    }

    public int getMaxIDJunk(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_ID +" FROM " + TABLE_CONTACTS_UNECNRYPTED +" WHERE " + KEY_JUNK_FLAG + "=1 ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor != null) {
            cursor.moveToFirst();
        }
        try {
            int ret = cursor.getInt(0);
            return ret;
        }catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            return -1;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }



}
