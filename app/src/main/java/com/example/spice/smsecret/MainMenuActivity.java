package com.example.spice.smsecret;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by spice on 10/08/16.
 */
public class MainMenuActivity extends Activity{
    private static MainMenuActivity ins;
    private static String password;
    MainMenuActivity getInstance(){
        return ins;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Prompting user to change to default SMS app
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getApplicationContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);


        ins = this;
        Button inboxButton = (Button) findViewById(R.id.buttonInbox);
        Button composeButton = (Button) findViewById(R.id.buttonCompose);
        Button buttonSettings = (Button) findViewById(R.id.buttonSettings);
        Log.d("DEBUG","ON Main Menu");
        inboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(), InboxActivity.class);
                startActivity(intent);
            }
        });

        composeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(),ComposeActivity.class);
                startActivity(intent);
            }
        });

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getInstance(),SettingsActivity.class);
                startActivity(intent);
            }
        });
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
