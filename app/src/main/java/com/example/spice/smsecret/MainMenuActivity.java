package com.example.spice.smsecret;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.CipherSuite;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.TlsVersion;
import com.squareup.okhttp.internal.framed.FrameReader;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by spice on 10/08/16.
 */
public class MainMenuActivity extends Activity{
    private static MainMenuActivity ins;
    private static String password;
    private TextView tvInboxUnread;
    private TextView tvSafeboxUnread;
    private TextView tvJunkUnread;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    MainMenuActivity getInstance(){
        return ins;
    }

    Vector<String> allSafeboxMessages;
    DatabaseHandler db;
    List<Contacts> listOfEncryptedMessages;


    ProgressDialog progressDialog;
    String contents;

    @Override
    protected void onResume() {
        super.onResume();
        getUnreadInboxMessages();
        getUnreadSafeboxMessages();
        getUnreadJunkMessages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu2);

        allSafeboxMessages = new Vector<>();
        db = new DatabaseHandler(this);
        listOfEncryptedMessages = db.getAllMessages();


        ins = this;
        TextView tvInbox = (TextView) findViewById(R.id.tvInbox);
        tvInboxUnread = (TextView) findViewById(R.id.tvInboxUnreadMessages);
        TextView tvSafebox = (TextView) findViewById(R.id.tvSafeBox);
        tvSafeboxUnread = (TextView) findViewById(R.id.tvSafeBoxUnreadMessages);
        TextView tvCompose = (TextView) findViewById(R.id.tvCompose);
        TextView tvJunk = (TextView) findViewById(R.id.tvJunk);
        tvJunkUnread = (TextView) findViewById(R.id.tvJunkUnreadMessages);
        TextView tvSettings = (TextView) findViewById(R.id.tvSettings);
        TextView tvWebapp = (TextView) findViewById(R.id.tvWebapp);
        Log.d("DEBUG","ON Main Menu");


/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    123);
        }
        else {

            String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(PERMISSIONS,10);

        }

*/

        //If authorisation not granted for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG_PERMISSION","No Camera & External_Storage permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Log.d("DEBUG_PERMISSION","No Camera permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Log.d("DEBUG_PERMISSION","No External_Storage permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }


        //Prompting user to change to default SMS app
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getApplicationContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);

        /*

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG_POST","No permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }

        */

        getUnreadInboxMessages();
        getUnreadSafeboxMessages();
        getUnreadJunkMessages();

        tvInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), UnencryptedInbox.class);
                startActivity(intent);
            }
        });

        tvJunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(),JunkInbox.class);
                startActivity(intent);
            }
        });

        tvCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(),ComposeActivity.class);
                startActivity(intent);
            }
        });

        tvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(),SettingsActivity.class);
                startActivity(intent);
            }
        });

        tvSafebox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG-Safe", "Clicked on SafeBox");
                Intent intent = new Intent(getInstance(),LoginActivity.class);
                startActivityForResult(intent,10);
            }
        });

        tvWebapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (shouldAskPermission() == true) {
                    String[] perms = {"Android.permission.WRITE_EXTERNAL_STORAGE"};
                    int permsRequestCode = 200;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d("DEBUG_POST", "Asking for permission");
                        ActivityCompat.requestPermissions(ins, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }

                }

                // Intent to the login
                Intent intent = new Intent(getInstance(),LoginActivity.class);
                startActivityForResult(intent,12);


            }





        });

/*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        },500);

*/

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getUnreadInboxMessages();
                                getUnreadSafeboxMessages();
                                getUnreadJunkMessages();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                } catch (Exception e){}
            }
        };

        t.start();
    }



    public class DecryptTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {


            List<Contacts> listOfMessages;
            Vector<String> allSafeboxMessagesDecrypted = null;
            Vector<String> allInboxMessages;

            for(int i=0; i<db.getMessagesCount(); i++){
                allSafeboxMessages.add(""+listOfEncryptedMessages.get(i).message);
            }

            try {
                allSafeboxMessagesDecrypted = decryptMessages
                        (allSafeboxMessages, MainActivity.getInstance().password);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }


            Map<String, String> paramsBox = new HashMap<>();

            int sizeOfDBSafeBox = 0;
            for(int i=0; i<db.getMessagesCount();i++){
                paramsBox.put("SAFEB"+String.valueOf(i),allSafeboxMessagesDecrypted.get(i));
                Log.d("DEBUG--->","Message #"+i+": "+allSafeboxMessagesDecrypted.get(i));
                paramsBox.put("SPHON"+String.valueOf(i),listOfEncryptedMessages.get(i).contactNumber);
                Log.d("DEBUG--->","Contact #"+i+": "+listOfEncryptedMessages.get(i).contactNumber);
                sizeOfDBSafeBox += 1;

                Log.d("DEBUG--->","Size of DB: "+sizeOfDBSafeBox);
            }


            // Iterate through the Inbox DB
            listOfMessages = db.getAllMessagesUnencrypted();
            allInboxMessages = new Vector<>();


            int sizeOfDBInbox = 0;
            for (int i=0; i<db.getMessagesCountUnencrypted(); i++){
                allInboxMessages.add(""+listOfMessages.get(i).message);
                paramsBox.put("INBOX"+String.valueOf(i),listOfMessages.get(i).message);
                paramsBox.put("IPHON"+String.valueOf(i),listOfMessages.get(i).contactNumber);
                sizeOfDBInbox += 1;
            }


            paramsBox.put("inboxSize",String.valueOf(sizeOfDBInbox));
            paramsBox.put("safeboxSize",String.valueOf(sizeOfDBSafeBox));
            paramsBox.put("Hash",contents);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject parameterBox = new JSONObject(paramsBox);
            RequestBody bodyBox = RequestBody.create(JSON, parameterBox.toString ());

            OkHttpClient client = new OkHttpClient();

            //For HTTPS
            String serverAddress = "http://192.168.1.78:800/JSONInbox";
            if (serverAddress.contains("https")) {
                trustEveryone();
                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256)
                        .supportsTlsExtensions(true)
                        .build();

                client.setConnectionSpecs(Collections.singletonList(spec));
                client.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                client.setConnectTimeout(1, TimeUnit.HOURS);
            }

            Request requestBox = new Request.Builder()
                    .url(serverAddress)
                    .addHeader("content-type", "application/json; charset=utf-8")
                    .post(bodyBox)
                    .build();




            client.newCall(requestBox).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e("response onFailure ", request.body().toString());
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    if(!response.isSuccessful()) throw new IOException(
                            "Unexpected Code: " + response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("DEBUG_POST","Report Received");
                            try {
                                Log.d("DEBUG_POST",""+response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            return 0;
        }
    }


    private boolean shouldAskPermission(){

        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == 11)
        {
            if (resultCode == RESULT_OK)
            {
                Log.d("DEBUG QR","Obtain QR Code");
                contents = data.getStringExtra("SCAN_RESULT");
                Toast.makeText(this, contents, Toast.LENGTH_SHORT).show();


                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(this.TELEPHONY_SERVICE);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                Map<String, String> params = new HashMap<>();

                String deviceID = tm.getDeviceId();
                Log.d("DEBUG_POST","HASH: "+contents);
                Log.d("DEBUG_POST","DEVICE ID: "+deviceID);


                // Save the Inbox Array to a JSON Object
                params.put("Hash",contents);
                params.put("ID", deviceID);

                OkHttpClient client = new OkHttpClient();


                String serverAddress = "http://192.168.1.78:800/JSONGetter";
                if (serverAddress.contains("https")) {
                    trustEveryone();
                    ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256)
                            .supportsTlsExtensions(true)
                            .build();

                    client.setConnectionSpecs(Collections.singletonList(spec));
                    client.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                    client.setConnectTimeout(1, TimeUnit.HOURS);
                }

                JSONObject parameterSession = new JSONObject(params);
                RequestBody bodySession = RequestBody.create(JSON, parameterSession.toString());

                Request requestSession = new Request.Builder()
                        .url(serverAddress)
                        .addHeader("content-type", "application/json; charset=utf-8")
                        .post(bodySession)
                        .build();


                client.newCall(requestSession).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e("response onFailure ", request.body().toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        if(!response.isSuccessful()) throw new IOException(
                                "Unexpected Code: " + response);
                        else{
                            //We have a valid Session, therefore we must save the Hash to the
                            // Internal Memory

                            File sessionHash = new File(ins.getFilesDir(),"session.hash");
                            sessionHash.createNewFile();
                            FileOutputStream stream = new FileOutputStream(sessionHash,false);
                            try{
                                stream.write(contents.getBytes());
                            } finally {
                                stream.close();
                            }


                            // Creating an Alarm Manager to keep checking the server
                            final AlarmManager alarmMgr =
                                    (AlarmManager)ins.getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(ins, AlarmReceiver.class);
                            intent.putExtra("hash", contents);
                            intent.putExtra("cancel","false");
                            PendingIntent pendingIntent =
                                    PendingIntent.getBroadcast(ins, 1001, intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
                            Calendar time = Calendar.getInstance();
                            time.setTimeInMillis(System.currentTimeMillis());
                            time.add(Calendar.SECOND, 5);
                            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), 1000*10,
                                    pendingIntent);

                            // Stopping the previous alarm after 4 minutes
                            final AlarmManager cancelAlarmMgr =
                                    (AlarmManager)ins.getSystemService(Context.ALARM_SERVICE);
                            Intent intentCancel = new Intent(ins, AlarmReceiver.class);
                            intentCancel.putExtra("cancel","true");
                            PendingIntent pendingIntentCancel =
                                    PendingIntent.getBroadcast(ins, 0, intent,
                                            PendingIntent.FLAG_ONE_SHOT);
                            time = Calendar.getInstance();
                            time.setTimeInMillis(System.currentTimeMillis());
                            time.add(Calendar.SECOND, 120*2);
                            cancelAlarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                                    pendingIntentCancel);

                            //Trying to read from file
                            int length = (int) sessionHash.length();

                            byte[] bytes = new byte[length];

                            FileInputStream in = new FileInputStream(sessionHash);
                            try {
                                in.read(bytes);
                            } finally {
                                in.close();
                            }

                            String contents = new String(bytes);

                            Log.d("DEBUG-FILE","Contents: "+contents);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("DEBUG_POST","Report Received");
                                try {
                                    Log.d("DEBUG_POST",""+response.body().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                });



                progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Decrypting Messages...");
                try {
                    progressDialog.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    new DecryptTask() {
                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            progressDialog.dismiss();
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                                MainMenuActivity.super.onDestroy();
                            }
                            MainMenuActivity.super.onDestroy();
                            if (o instanceof String) {
                                Toast.makeText(getApplicationContext(), "Unexpected error: " + o, Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(getApplicationContext(), "Decryption Successful", Toast.LENGTH_SHORT);
                        }

                    }.execute();
                }catch (Exception e){
                    e.printStackTrace();
                }


            } else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "Not proper QRCODE...!",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 10) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("DEBUG", "Activity Result");
                MainActivity.getInstance().password = data.getStringExtra("result");
                Intent intent2 = new Intent(this, EncryptedInbox.class);
                startActivityForResult(intent2, 9);
            }
        }

        if(requestCode == 12){
            try {
                MainActivity.getInstance().password = data.getStringExtra("result");
            }catch (Exception e){
                Log.d("DEBUG-PASSWORD","No password provided.");
                return;
            }

            IntentIntegrator integrator = new IntentIntegrator(ins);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setScanningRectangle(700,700);
            integrator.setPrompt("Faça Scan do QR Code");
            integrator.setResultDisplayDuration(0);
            integrator.setCameraId(0);  // Use a specific camera of the device
            Intent integratorIntent = integrator.createScanIntent();

            //integrator.initiateScan();
            startActivityForResult(integratorIntent, 11);
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


    public Vector<String> decryptMessages(Vector<String> messagesVector, String password) throws IOException, ClassNotFoundException, GeneralSecurityException {
        RSA rsa = new RSA();
        String currentMessage = "";
        String salt = readFromFile("AES.salt");
        AES.SecretKeys keys = rsa.genKeyWithSalt(password,salt);
        File privateKeyFile = new File(this.getFilesDir(),"private.key");
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(privateKeyFile));
        AES.CipherTextIvMac cipheredPrivateKey = (AES.CipherTextIvMac) inputStream.readObject();
        byte[] privateKeyEncoded = AES.decrypt(cipheredPrivateKey,keys);
        byte[] privateKeyDecoded = Base64.decode(privateKeyEncoded,Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyDecoded);
        PrivateKey privateKey = kf.generatePrivate(pkcs8EncodedKeySpec);
        byte[] messageDecoded;
        Vector<String> decryptedMessages = new Vector<>();
        for (int i = 0; i < messagesVector.size(); i++) {
            currentMessage = messagesVector.elementAt(i);
            messageDecoded = Base64.decode(currentMessage.getBytes(),Base64.DEFAULT);
            decryptedMessages.add(rsa.decrypt(messageDecoded,privateKey));
        }
        return decryptedMessages;
    }

    public void getUnreadInboxMessages(){
        DatabaseHandler db = new DatabaseHandler(this);
        tvInboxUnread.setText(""+db.getNumberOfUnvisitedUnencrypted());
    }

    public void getUnreadSafeboxMessages(){
        DatabaseHandler db = new DatabaseHandler(this);
        tvSafeboxUnread.setText(""+db.getNumberOfUnvisited());
    }

    public void getUnreadJunkMessages(){
        DatabaseHandler db = new DatabaseHandler(this);
        tvJunkUnread.setText(""+db.getNumberOfUnvisitedJunk());
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //getInstance().finish();
        //System.exit(0);
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
