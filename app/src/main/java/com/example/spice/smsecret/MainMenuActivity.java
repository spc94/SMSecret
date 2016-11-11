package com.example.spice.smsecret;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
        ins = this;
        Button inboxButton = (Button) findViewById(R.id.buttonInbox);
        Button composeButton = (Button) findViewById(R.id.buttonCompose);
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
