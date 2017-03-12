package com.example.spice.smsecret;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

/**
 * Created by spice on 04/08/16.
 */
public class SetPasswordActivity extends Activity {

    public static final String ALGORITHM = "RSA";
    public static final String PRIVATE_KEY_FILE = "private.key";
    public static final String PUBLIC_KEY_FILE = "public.key";

    public LinearLayout setPasswordLayout;
    public static SetPasswordActivity ins;

    public static SetPasswordActivity getInstance(){
        return ins;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_set_password);
        setPasswordLayout = (LinearLayout)findViewById(R.id.activity_set_password);

        Switch passwordSwitch = (Switch) findViewById(R.id.switchVisibility);
        final EditText password = (EditText) findViewById(R.id.password);
        final EditText passwordConfirmation = (EditText) findViewById(R.id.passwordConfirmation);


        passwordConfirmation.setOnTouchListener( new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                passwordConfirmation.setHint("");
                return false;
            }

        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    password.setHint("Password");
                }
            }
        });

        password.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                password.setHint("");
                return false;
            }

        });

        passwordConfirmation.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    passwordConfirmation.setHint("Password Confirmation");
                }
            }
        });

        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordConfirmation.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                else{
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordConfirmation.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        passwordConfirmation.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // TODO do something
                    if (password.getText().toString().contentEquals(passwordConfirmation.getText().toString())){
                        Log.d("DEBUG","Password is the same");
                        try {
                            generateSystemFiles(password.getText().toString());
                            //Exit Fragment
                            Log.d("DEBUG","Everything generated");
                            MainActivity.getInstance().loginComplete=true;
                            //MainActivity.getInstance().password = password.getText().toString();


                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result",password.getText().toString());
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();

                            //doRestart(getApplicationContext());
                            Log.d("DEBUG","Fragment Exited?");
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Log.d("DEBUG", "Passwords are different");
                        Toast.makeText(getApplicationContext(), "The passwords don't match", Toast.LENGTH_SHORT).show();
                        handled = true;
                    }
                }
                return handled;
            }

        });



    }

    public static void doRestart(Context c){
        try{
            if(c!=null){
                PackageManager pm = c.getPackageManager();

                if(pm!=null){
                    Intent mStartActivity = pm.getLaunchIntentForPackage(c.getPackageName());

                    if(mStartActivity !=null){
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        int mPendingIntentId = 22344;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(c,
                                mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis()+500, mPendingIntent);

                        System.exit(0);

                    } else{Log.e("DEBUG","Was not able to restart app, mStart null");

                    }
                }else{
                    Log.e("DEBUG","Was not able to restart app, PM null");
                }
            }else{
                Log.e("DEBUG","Was not able to restart app, Context null");
            }
        }catch (Exception ex){
            Log.e("DEBUG","Was not able to restart app");
        }
    }

    public void cleanLayout(){
        setPasswordLayout.removeAllViews();
    }

    public void setPasswordInit(){
        cleanLayout();
    }

    public void generateSystemFiles(String password) throws NoSuchAlgorithmException, IOException {
        RSA rsa = new RSA();
        //Generate RSA PublicKey-PrivateKey Pair
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(1024);
        final KeyPair key = keyGen.generateKeyPair();

        //Creates files for private key and public key
        File privateKeyFile = new File(this.getFilesDir(),PRIVATE_KEY_FILE);
        File publicKeyFile = new File(this.getFilesDir(),PUBLIC_KEY_FILE);
        privateKeyFile.createNewFile();
        publicKeyFile.createNewFile();

        //Initializes ObjectOutputStream for Public Key and Private Key
        ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                new FileOutputStream(publicKeyFile));

        ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                new FileOutputStream(privateKeyFile));

        //PUBLIC KEY
        //Saves Public Key Object to File
        publicKeyOS.writeObject(key.getPublic());
        publicKeyOS.close();

        //PRIVATE KEY
        //Generate AES-256 keys with the user password and creates AES.salt file
        AES.SecretKeys keys = rsa.genKeys(password);

        //Encodes Private Key to Base64, so it can be encrypted using AES-256 and saves it to String
        PrivateKey privateKey = key.getPrivate();
        byte[] k = Base64.encode(privateKey.getEncoded(),Base64.DEFAULT);
        String encodedPrivateKey = new String(k);

        //Creates a cipher object using the AES-256 Keys and the Base64-encoded Private Key String
        AES.CipherTextIvMac cipher = rsa.genCipher(encodedPrivateKey,keys);

        //Write Cipher Object to File
        privateKeyOS.writeObject(cipher);
        privateKeyOS.close();
    }

    public void writeToFile(String fileName, String contents){
        File file = new File(this.getFilesDir(),fileName);
        FileOutputStream outputStream;

        try {
            outputStream = this.openFileOutput(fileName, Context.MODE_PRIVATE);
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
            inputStream = this.openFileInput(fileName);
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
}
