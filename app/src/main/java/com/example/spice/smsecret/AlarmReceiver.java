package com.example.spice.smsecret;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by spice on 6/20/17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    String output;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("DEBUG-ALARM", "We're at the onReceive Alarm Method");
        Log.d("DEBUG-ALARM", "These are the contents of the extra: " + intent.getStringExtra("hash"));

        // Check if Alarm Cancel or Not

        if (intent.getStringExtra("cancel").equals("true")) {
            Log.d("DEBUG-ALARM", "Alarm Cancelled");
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1001, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            manager.cancel(alarmIntent);
            alarmIntent.cancel();
            return;
        } else {

            // Trying to obtain JSON from UNENCRYPTED PAGE
            try {
                URL url = new URL("http://192.168.1.78:800/Instructions/deleteSmsUnenc?Hash=" + intent.getStringExtra("hash"));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder total = new StringBuilder(in.available());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    output = total.toString();
                    Log.d("DEBUG-JSON", "Page contents: " + output);
                } finally {
                    urlConnection.disconnect();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject json = new JSONObject(output);


                String s = json.getJSONArray("id").get(0).toString();
                for (int i = 0; i < json.getJSONArray("id").length(); i++) {
                    Log.d("DEBUG-JSON", "String contents: " + json.getJSONArray("id").get(i).toString());

                    // Delete from Android Database the Following Id
                    DatabaseHandler db = new DatabaseHandler(context);
                    db.deleteNTHRowUnencrypted(Integer.parseInt(json.getJSONArray("id").get(i).toString()));

                    Log.d("DEBUG-JSON", "Row deleted from the DB");

                    // Show a notification for the deletion
                    showNotification(context, "Inbox", "delete");
                }

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            } catch (Exception e){
                e.printStackTrace();
            }

            // Trying to obtain JSON from ENCRYPTED PAGE

            try {
                URL url = new URL("http://192.168.1.78:800/Instructions/deleteSmsEnc?Hash=" + intent.getStringExtra("hash"));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder total = new StringBuilder(in.available());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    output = total.toString();
                    Log.d("DEBUG-JSON", "Page contents: " + output);
                } finally {
                    urlConnection.disconnect();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject json = new JSONObject(output);


                String s = json.getJSONArray("id").get(0).toString();
                for (int i = 0; i < json.getJSONArray("id").length(); i++) {
                    Log.d("DEBUG-JSON", "String contents: " + json.getJSONArray("id").get(i).toString());

                    // Delete from Android Database the Following Id
                    DatabaseHandler db = new DatabaseHandler(context);
                    db.deleteNTHRowEncrypted(Integer.parseInt(json.getJSONArray("id").get(i).toString()));

                    Log.d("DEBUG-JSON", "Row deleted from the DB");

                    // Show a notification for the deletion
                    showNotification(context, "SafeBox", "delete");
                }

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            } catch (Exception e){
                e.printStackTrace();
            }


            // Trying to obtain the JSON from SEND SMS Page

            try {
                URL url = new URL("http://192.168.1.78:800/Instructions/sendSms?Hash=" + intent.getStringExtra("hash"));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder total = new StringBuilder(in.available());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    output = total.toString();
                    Log.d("DEBUG-JSON", "Page contents: " + output);
                } finally {
                    urlConnection.disconnect();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject json = new JSONObject(output);


                for (int i = 0; i < json.getJSONArray("phone").length(); i++) {
                    Log.d("DEBUG-JSON", "Phone contents: " + json.getJSONArray("phone").get(i).toString());
                    Log.d("DEBUG-JSON", "Message contents: " + json.getJSONArray("message").get(i).toString());
                    // Send SMS
                    smsSender(json.getJSONArray("phone").get(i).toString(),
                            json.getJSONArray("message").get(i).toString(),
                            context);

                    Log.d("DEBUG-JSON", "SMS Sent");

                    // Show a notification for SMS Sent
                    showNotification(context, ""+json.getJSONArray("phone").get(i).toString(), "send");
                }

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            } catch (Exception e){
                e.printStackTrace();
            }



        }
    }

    public void smsSender(String phone, String message, Context context){
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);


        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, message, sentPI, deliveredPI);

    }


    public void showNotification(Context context, String box, String operation){
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher_white)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.mipmap.ic_launcher))
                        .setColor(Color.rgb(218, 62, 75))
                        .setContentTitle("SMSecret")
                        .setContentText("Message deleted from the " + box)
                        .setSound(notificationSound)
                        .setAutoCancel(true);

        if(operation.equals("delete")) {
            mBuilder.setContentText("Message deleted from the " + box);
        }
        if(operation.equals("send")){
            mBuilder.setContentText("Message sent to " + box);
        }
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(002, mBuilder.build());
    }
}
