package com.example.spice.smsecret;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by spice on 10/08/16.
 */
public class MainMenuActivity extends Activity{
    private static MainMenuActivity ins;
    private static String password;
    private TextView tvInboxUnread;
    private TextView tvSafeboxUnread;
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
        Log.d("DEBUG","ON Main Menu");

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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK) {
            Log.d("DEBUG","Activity Result");
            MainActivity.getInstance().password = data.getStringExtra("result");
            Intent intent2 = new Intent(this, EncryptedInbox.class);
            startActivityForResult(intent2,9);
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
