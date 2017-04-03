package com.example.spice.smsecret;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.spice.smsecret.DAL.WhitelistedNumbersDAL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.PublicKey;
import java.util.Vector;

/**
 * Created by spice on 31/07/16.
 */
public class SmsReceiver extends BroadcastReceiver {
    Vector<Contacts> contactsVector = new Vector<>();

    private static SmsReceiver ins;
    public SmsReceiver getInstance(){
        return ins;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG_MODE","Checking SMS");
        ins=this;
        Bundle bundle = intent.getExtras();
        DatabaseHandler db = new DatabaseHandler(context);
        try {

            if (bundle != null) {
                String senderNumber = null;
                String msg = "";

                if (Build.VERSION.SDK_INT >= 19) { //KITKAT

                    SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                    for(int i=0;i<msgs.length;i++)
                        msg += msgs[i].getDisplayMessageBody();
                    senderNumber = msgs[0].getOriginatingAddress();
                } else {
                    Object pdus[] = (Object[]) bundle.get("pdus");
                    for(int i=0;i<pdus.length;i++)
                        msg += SmsMessage.createFromPdu((byte[]) pdus[i]).getDisplayMessageBody();
                    senderNumber = SmsMessage.createFromPdu((byte[])pdus[0]).getOriginatingAddress();
                }
                if(checkContactWhitelisted(Integer.parseInt(senderNumber))) {
                    abortBroadcast();
                    Log.d("DEB", "From: " + senderNumber + "\nMsg: " + msg);
                    Log.d("DEB", "Root of Files: " + context.getFilesDir().getAbsolutePath());
                    msg = encRSA(msg, context);
                    Contacts contact = new Contacts(Integer.parseInt(senderNumber), msg, 0);
                    Log.d("DEB2", String.valueOf(contact.getContactNumber()));
                    Log.d("DEB2", contact.getMessages());

                    db.addMessage(contact);
                    db.close();


                    //Reads database into textView array
                    InboxActivity.getInstance().textView = InboxActivity.getInstance().populateTextViewArray();
                    //Clears all the textViews
                    InboxActivity.getInstance().cleanLayout();
                    InboxActivity.getInstance().tvWelcome.setVisibility(View.INVISIBLE);
                    //Writes everything from the textView array to the layout
                    InboxActivity.getInstance().addContactsToLayout(InboxActivity.getInstance().contactsLayout,
                            InboxActivity.getInstance().textView, InboxActivity.getInstance().sizeOfTextViewArray);
                    InboxActivity.getInstance().initTextViewListeners();
                    MessagesActivity.getInstance().initMessages(contact.getContactNumber());
                }
                else{
                    //Intent newIntent = new Intent();
                    Intent redirectIntent = intent;
                    redirectIntent.setClassName("com.android.mms.transaction","com.android.mms.transaction.SmsReceiver");
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d("DEBUG","Enters else");
                    context.startActivity(redirectIntent);
                    //context.startActivity(redirectIntent);
                    //context.sendOrderedBroadcast(redirectIntent,null);
                }

            }
        }catch (Exception e){
            Log.e("DEBUG_MODE","Exception");
            e.printStackTrace();
        }
    }

    public boolean checkContactWhitelisted(int senderNumber){
        java.util.List<String> listOfWhitelistedContacts = WhitelistedNumbersDAL.getAllWhitelistedNumbers();
        for(int i=0; i<listOfWhitelistedContacts.size();i++){
            if(listOfWhitelistedContacts.get(i).equals(""+senderNumber))
                return true;
        }
        return false;
    }

    public String encRSA(String msg, Context context) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = null;
        RSA rsa = new RSA();
        //Read Public key from File
        File publicKeyFile = new File(context.getFilesDir(),"public.key");
        inputStream = new ObjectInputStream(new FileInputStream(publicKeyFile));
        final PublicKey publicKey = (PublicKey) inputStream.readObject();

        final byte[] cipherText = rsa.encrypt(msg, publicKey);
        byte[] cipherTextEncoded = Base64.encode(cipherText,Base64.DEFAULT);
        String cipherTextString = new String(cipherTextEncoded);

        return cipherTextString;

    }

    public boolean checkContactExists(int senderNumber){
        for (int i = 0; i < contactsVector.size(); i++) {
            if(senderNumber == contactsVector.elementAt(i).getContactNumber())
                return true;
        }
        return false;
    }

    public void addContact(int senderNumber, String message){
        Contacts contact = new Contacts(senderNumber,message,0);
        contactsVector.add(contact);
    }


    private class List<T> {
    }
}
