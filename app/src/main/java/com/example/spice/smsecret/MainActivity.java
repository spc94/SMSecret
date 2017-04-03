package com.example.spice.smsecret;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends FragmentActivity{
    private static MainActivity ins;



    public String password = null;
    public boolean loginComplete = false;
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_main);
        //Log.d("DEBUG PACKAGE ",""+Telephony.Sms.getDefaultSmsPackage(getApplicationContext()));
        //loginComplete = p.getBoolean("loginFlag");
        if(loadSystemFiles()==false) {
            Intent intent = new Intent(this, SetPasswordActivity.class);
            Log.d("DEBUG","Before start");
            startActivityForResult(intent,10);
            //startActivity(intent);
            password = intent.getStringExtra("result");
            /*Log.d("DEBUG","XXXX - "+password);
            intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent,10);*/
        }
        //If opening app for the first time, it won't request login
        else if(true) {
            Log.d("DEBUG","Enter Login Activity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent,10);
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK) {
            Log.d("DEBUG","Activity Result");
            this.password = data.getStringExtra("result");
            Intent intent2 = new Intent(this, MainMenuActivity.class);
            startActivityForResult(intent2,9);
        }
    }

    public void removeAllDBEntries(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        int size = db.getMessagesCount();
        Log.d("DEBUG","DB SIZE: "+size);
        for (int i = 2; i < size; i++) {
            db.deleteMessage(i);
        }
    }

    public boolean loadSystemFiles(){
        if(checkSystemFileExists("public.key")==true &&
                checkSystemFileExists("private.key")==true &&
                checkSystemFileExists("AES.salt")==true){
            return true;
        }
        else
            return false;
    }

    public boolean checkSystemFileExists(String fileName){
        File file = new File(this.getFilesDir(),fileName);
        return file.exists();
    }

    public void removeFile(String fileName){
        File file = new File(this.getFilesDir(),fileName);
        file.delete();
    }

    public void writeToFile(String fileName, String contents){
        File file = new File(this.getFilesDir(),fileName);
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(contents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFromFile(String fileName){
        File file = new File(this.getFilesDir(),fileName);
        FileInputStream inputStream;
        String outputString = null;

        try {
            int n;
            inputStream = openFileInput(fileName);
            StringBuffer fileContents = new StringBuffer("");
            byte [] buffer = new byte[1024];
            while ((n = inputStream.read(buffer))!= -1)
                fileContents.append(new String(buffer, 0, n));

            inputStream.close();
            outputString = fileContents.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputString;
    }

        //Get this very instance of MainActivity on other classes
    public static MainActivity getInstance(){
        return ins;
    }







}
