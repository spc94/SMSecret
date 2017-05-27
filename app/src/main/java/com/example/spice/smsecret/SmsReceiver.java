package com.example.spice.smsecret;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
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

import br.com.goncalves.pugnotification.notification.PugNotification;

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

                senderNumber = countryDialingCodeToZeroes(senderNumber);
                Log.d("DEBUG","Number: "+senderNumber);
                if(checkContactWhitelisted(senderNumber)) {
                    Log.d("DEBUG","ENTERS IF");
                    abortBroadcast();
                    Log.d("DEB", "From: " + senderNumber + "\nMsg: " + msg);
                    Log.d("DEB", "Root of Files: " + context.getFilesDir().getAbsolutePath());
                    msg = encRSA(msg, context);
                    Contacts contact = new Contacts(senderNumber, msg, 0);
                    Log.d("DEB2", String.valueOf(contact.getContactNumber()));
                    Log.d("DEB2", contact.getMessages());

                    db.addMessage(contact);
                    db.close();


                    //Reads database into textView array
                    EncryptedInbox.getInstance().textView = EncryptedInbox.getInstance().populateTextViewArray();
                    //Clears all the textViews
                    EncryptedInbox.getInstance().cleanLayout();
                    EncryptedInbox.getInstance().tvWelcome.setVisibility(View.INVISIBLE);
                    //Writes everything from the textView array to the layout
                    EncryptedInbox.getInstance().addContactsToLayout(EncryptedInbox.getInstance().contactsLayout,
                            EncryptedInbox.getInstance().textView, EncryptedInbox.getInstance().sizeOfTextViewArray);
                    EncryptedInbox.getInstance().initTextViewListeners();
                    EncryptedMessages.getInstance().initMessages(contact.getContactNumber());
                }
                else{
                    Log.d("DEBUG","ENTERS ELSE");
                    abortBroadcast();
                    Log.d("DEB", "From: " + senderNumber + "\nMsg: " + msg);
                    Log.d("DEB", "Root of Files: " + context.getFilesDir().getAbsolutePath());
                    Contacts contact = new Contacts(senderNumber, msg, 0);
                    Log.d("DEB2", String.valueOf(contact.getContactNumber()));
                    Log.d("DEB2", contact.getMessages());

                    db.addMessageUnencrypted(contact);
                    db.close();

                    //Reads database into textView array
                    UnencryptedInbox.getInstance().textView = UnencryptedInbox.getInstance().populateTextViewArray();
                    //Clears all the textViews
                    UnencryptedInbox.getInstance().cleanLayout();
                    UnencryptedInbox.getInstance().tvWelcome.setVisibility(View.INVISIBLE);
                    //Writes everything from the textView array to the layout
                    UnencryptedInbox.getInstance().addContactsToLayout(UnencryptedInbox.getInstance().contactsLayout,
                            UnencryptedInbox.getInstance().textView, UnencryptedInbox.getInstance().sizeOfTextViewArray);
                    UnencryptedInbox.getInstance().initTextViewListeners();
                    EncryptedMessages.getInstance().initMessages(contact.getContactNumber());

/*
                    PugNotification.with(context)
                            .load()
                            .title("From: "+senderNumber)
                            .message(""+msg)
                            .bigTextStyle("Sample")
                            .smallIcon(R.drawable.pugnotification_ic_launcher)
                            .largeIcon(R.drawable.pugnotification_ic_launcher)
                            .flags(Notification.DEFAULT_ALL)
                            .simple()
                            .build();
                            */


                    //Notification for SMS
/*
                    Notification notif = new Notification.Builder(context)
                            .setContentTitle("New mail from " )
                            .setContentText("KEK")
                            .setSmallIcon(R.drawable.pugnotification_ic_launcher)
                            .setStyle(new Notification.BigTextStyle()
                                    .bigText("a very looooooooooooooooooong string"))
                            .build();
*/
/*
                    Intent inboxIntent = new Intent(context, UnencryptedInbox.class);
                    PendingIntent pIntent = PendingIntent.getActivity(context, 0, inboxIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.circle_blue)
                            .setContentTitle(""+senderNumber)
                            .setContentText(""+msg)
                            .setContentIntent(pIntent)
                            .setStyle(new Notification.BigTextStyle().bigText("STRING STRING STRING SOME MORE STRING"))
                            .setAutoCancel(true);

                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0,mBuilder.build());
*/

                }

            }
        }catch (Exception e){
            Log.e("DEBUG_MODE","Exception");
            e.printStackTrace();
        }


    }

    public String countryDialingCodeToZeroes(String number){

        number.replaceAll(" ","");

        if(number.charAt(0)=='+')
            return "00" + number.substring(1);
        else
            return number;
    }

    public boolean checkContactWhitelisted(String senderNumber){
        java.util.List<String> listOfWhitelistedContacts = WhitelistedNumbersDAL.getAllWhitelistedNumbers();
        //Removing the country code
        String strippedNumber = senderNumber.substring(5);
        Log.d("DEB3","Number: "+senderNumber);
        Log.d("DEB3","Stripped: "+strippedNumber);
        for(int i=0; i<listOfWhitelistedContacts.size();i++){
            if(listOfWhitelistedContacts.get(i).equals(senderNumber) ||
                    listOfWhitelistedContacts.get(i).equals(strippedNumber))

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

    public boolean checkContactExists(String senderNumber){
        for (int i = 0; i < contactsVector.size(); i++) {
            if(senderNumber.equals(contactsVector.elementAt(i).getContactNumber()))
                return true;
        }
        return false;
    }

    public void addContact(int senderNumber, String message){
        Contacts contact = new Contacts(String.valueOf(senderNumber),message,0);
        contactsVector.add(contact);
    }


    private class List<T> {
    }
}
