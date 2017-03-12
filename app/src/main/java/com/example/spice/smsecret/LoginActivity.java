package com.example.spice.smsecret;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;

/**
 * Created by spice on 04/08/16.
 */
public class LoginActivity extends Activity {

    private static LoginActivity ins;
    public String randomString = "";
    public LoginActivity getInstance(){
        return ins;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_login);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.activity_login);
        final EditText passwordEditText = (EditText) findViewById(R.id.loginPassword);
        Log.d("DEBUG","On Login");
        passwordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean ret = false;

                try {
                    if(passwordCheck(passwordEditText.getText().toString()
                            ,generateFileToDecrypt(),randomString)==true) {
                        //MainActivity.getInstance().password = passwordEditText.getText().toString();
                        MainActivity.getInstance().loginComplete=true;
                        runOnUiThread(new Runnable(){
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Welcome back :)",Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result",passwordEditText.getText().toString());
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                    else{
                        //passwordEditText.setText("");
                        //Toast.makeText(getInstance().getApplicationContext(),"The password is wrong.\nTry again.", Toast.LENGTH_LONG);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                   // passwordEditText.setText("");
                    Log.d("DEBUG","CATCH METHOD");
                    runOnUiThread(new Runnable(){
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Wrong password. Try again :)",Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    return false;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public byte[] generateFileToDecrypt() throws IOException, ClassNotFoundException {
        File publicKeyFile = new File(getInstance().getFilesDir(),"public.key");
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(publicKeyFile));
        PublicKey publicKey = (PublicKey) objectInputStream.readObject();
        this.randomString = randString();
        Log.d("DEBUG","String created: "+randomString);
        return RSA.encrypt(randomString,publicKey);
    }

    public boolean passwordCheck(String password, byte[] cipheredText, String randomString) throws IOException, GeneralSecurityException, ClassNotFoundException {
        String saltString = MainActivity.getInstance().readFromFile("AES.salt");
        RSA rsa = new RSA();
        AES.SecretKeys keys = rsa.genKeyWithSalt(password,saltString);
        File privateKeyFile = new File(this.getFilesDir(),"private.key");
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(privateKeyFile));
        AES.CipherTextIvMac cipheredPrivateKey = (AES.CipherTextIvMac) inputStream.readObject();
        byte[] privateKeyEncoded = AES.decrypt(cipheredPrivateKey,keys);
        byte[] privateKeyDecoded = Base64.decode(privateKeyEncoded,Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyDecoded);
        PrivateKey privateKey = kf.generatePrivate(pkcs8EncodedKeySpec);
        String textDecrypted = rsa.decrypt(cipheredText,privateKey);
        Log.d("DEBUG","Original Text: "+randomString);
        Log.d("DEBUG","Text Decrypted: "+textDecrypted);
        if(textDecrypted.contentEquals(randomString)) {
            Log.d("DEBUG","Decrypted");
            return true;
        }
        else {
            Log.d("DEBUG","False returned");
            return false;
        }
    }

    public String randString(){

        StringBuilder sb = new StringBuilder();
        Random randSize = new Random();
        Random randChar = new Random();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz";
        int stringSize;

            stringSize = randSize.nextInt(32)+32;
            for(int j=0;j<stringSize;j++){
                sb.append(alphabet.charAt(randChar.nextInt(52)));
            }

        return sb.toString();
    }
}
