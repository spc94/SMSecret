package com.example.spice.smsecret;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Vector;

/**
 * Created by spice on 02/08/16.
 */


public class MessagesActivity extends Activity {
    public LinearLayout messagesLayout;
    private static MessagesActivity ins;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_messages);
        messagesLayout = (LinearLayout)findViewById(R.id.messages);
        //Obtain extra parameters
        Bundle b = getIntent().getExtras();
        //Assigned passed parameter to a var
        int contactNumber = b.getInt("contact");

        DatabaseHandler db = new DatabaseHandler(this);
        db.changeVisitedTrue(contactNumber);

        try {
            initMessages(contactNumber);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cleanLayout(){
        messagesLayout.removeAllViews();
    }

    public void initMessages(int contactNumber) throws GeneralSecurityException, IOException, ClassNotFoundException {
        //Cleans Layout
        cleanLayout();
        //Fills a Vector with all messages from specified contact
        Vector<String> messagesVector = getMessagesFromContact(contactNumber);
        //Decrypts messages
        messagesVector = decryptMessages(messagesVector,MainActivity.getInstance().password);
        //Copies messages from vector to array of text view
        TextView [] tvArray = populateTextViewArray(messagesVector);
        //Adds Messages to Layout
        addMessagesToLayout(tvArray);
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

    public void addMessagesToLayout(TextView[] tvArray){
        for (int i = tvArray.length-1; i >= 0; i--) {
            String temp = tvArray[i].getText().toString();
            tvArray[i].setTextSize(24);
            tvArray[i].setText(temp + "\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            messagesLayout.addView(tvArray[i]);
        }
    }

    public TextView[] populateTextViewArray(Vector<String> v){
        int size = v.size();
        final TextView[] tvArray = new TextView[size];

        for (int i = 0; i < size; i++) {
            final TextView t = new TextView(this);
            t.setText(v.elementAt(i));
            tvArray[i] = t;
        }
        return tvArray;
    }

    public Vector<String> getMessagesFromContact(int contact){
        Vector<String> v = new Vector<>();
        List<Contacts> list =  InboxActivity.getInstance().db.getAllMessages();
        for (int i = 0; i < list.size(); i++) {
            if(contact == list.get(i).getContactNumber())
                v.add(list.get(i).getMessages());
        }
        return v;
    }



    public static MessagesActivity getInstance(){
        return ins;
    }
}
