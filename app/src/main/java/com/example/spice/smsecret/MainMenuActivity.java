package com.example.spice.smsecret;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by spice on 10/08/16.
 */
public class MainMenuActivity extends Activity{
    private static MainMenuActivity ins;
    private static String password;
    private TextView tvInboxUnread;
    private TextView tvSafeboxUnread;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    MainMenuActivity getInstance(){
        return ins;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUnreadInboxMessages();
        getUnreadSafeboxMessages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu2);


        //Prompting user to change to default SMS app
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getApplicationContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);


        ins = this;
        TextView tvInbox = (TextView) findViewById(R.id.tvInbox);
        tvInboxUnread = (TextView) findViewById(R.id.tvInboxUnreadMessages);
        TextView tvSafebox = (TextView) findViewById(R.id.tvSafeBox);
        tvSafeboxUnread = (TextView) findViewById(R.id.tvSafeBoxUnreadMessages);
        TextView tvCompose = (TextView) findViewById(R.id.tvCompose);
        TextView tvJunk = (TextView) findViewById(R.id.tvJunk);
        TextView tvJunkUnread = (TextView) findViewById(R.id.tvJunkUnreadMessages);
        TextView tvSettings = (TextView) findViewById(R.id.tvSettings);
        TextView tvWebapp = (TextView) findViewById(R.id.tvWebapp);
        Log.d("DEBUG","ON Main Menu");

        //If authorisation not granted for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG_POST","No permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG_POST","No permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }

        getUnreadInboxMessages();
        getUnreadSafeboxMessages();

        tvInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), UnencryptedInbox.class);
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





        });
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
                String contents = data.getStringExtra("SCAN_RESULT");
                Toast.makeText(this, contents, Toast.LENGTH_SHORT).show();


                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(this.TELEPHONY_SERVICE);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                Map<String, String> params = new HashMap<String, String>();
                String deviceID = tm.getDeviceId();
                Log.d("DEBUG_POST","HASH: "+contents);
                Log.d("DEBUG_POST","DEVICE ID: "+deviceID);
                params.put("Hash",contents);
                params.put("ID", deviceID);
                JSONObject parameter = new JSONObject(params);
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, parameter.toString());

                Request request = new Request.Builder()
                        .url("http://192.168.1.15:800/JSONGetter")
                        .addHeader("content-type", "application/json; charset=utf-8")
                        .post(body)
                        .build();
                /*

                 Request request = new Request.Builder()
                        .url("http://192.168.43.189:800/JSONGetter")
                        .get()
                        .addHeader("content-type", "application/json; charset=utf-8")
                        .build();
                 */
                /*try {
                    Log.d("DEBUG_POST","Trying to EXECUTE");
                    //Response response = client.newCall(request).execute();
                    Log.d("DEBUG_POST","Ola"+response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                client.newCall(request).enqueue(new Callback() {
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
    }

    public void getUnreadInboxMessages(){
        DatabaseHandler db = new DatabaseHandler(this);
        tvInboxUnread.setText(""+db.getMessagesCountUnencrypted());
    }

    public void getUnreadSafeboxMessages(){
        DatabaseHandler db = new DatabaseHandler(this);
        tvSafeboxUnread.setText(""+db.getMessagesCount());
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
}
