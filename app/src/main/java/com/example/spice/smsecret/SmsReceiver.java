package com.example.spice.smsecret;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import java.util.HashMap;
import java.util.Map;
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
                    msg = encRSA(msg, context);
                    Contacts contact = new Contacts(senderNumber, msg, 0, 0);

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
                    Map<String, String> namePhoneCorrespondence = getContactCorrespondence(context);
                    String notificationSenderNumber = senderNumber;

                    //Check if contact exists on PhoneBook
                    int junkFlag = 1;

                    if(phoneNumberExists(namePhoneCorrespondence,senderNumber))
                        junkFlag = 0;

                    Log.d("DEBUG-JUNK","FLAG: "+junkFlag);
                    Contacts contact = new Contacts(senderNumber, msg, 0, junkFlag);

                    db.addMessageUnencrypted(contact);
                    db.close();

                    Intent notificationIntent;


                    //If it is a Junk Message, the user will be taken to the Junk Inbox
                    //Else, the user will be taken to the regular inbox
                    if(junkFlag == 1)
                        notificationIntent = new Intent(context,JunkInbox.class);
                    else
                        notificationIntent = new Intent(context,UnencryptedInbox.class);

                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent notificationPendingIntent =
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_ONE_SHOT
                            );




                    if(phoneNumberExists(namePhoneCorrespondence,senderNumber))
                        notificationSenderNumber = namePhoneCorrespondence.get(senderNumber);
                    Log.d("DEBUG-NOTI",notificationSenderNumber);
                    Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_launcher_white)
                                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                            R.mipmap.ic_launcher))
                                    .setColor(Color.rgb(218,62,75))
                                    .setContentTitle("From: "+notificationSenderNumber)
                                    .setContentText(""+msg)
                                    .setSound(notificationSound)
                                    .setAutoCancel(true)
                                    .setContentIntent(notificationPendingIntent);
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(001, mBuilder.build());


                    //Reads database into textView array
                    UnencryptedInbox.getInstance().textView = UnencryptedInbox.getInstance().populateTextViewArray();
                    //Clears all the textViews
                    UnencryptedInbox.getInstance().cleanLayout();
                    UnencryptedInbox.getInstance().tvWelcome.setVisibility(View.INVISIBLE);
                    //Writes everything from the textView array to the layout
                    UnencryptedInbox.getInstance().addContactsToLayout(UnencryptedInbox.getInstance().contactsLayout,
                            UnencryptedInbox.getInstance().textView, UnencryptedInbox.getInstance().sizeOfTextViewArray);
                    UnencryptedInbox.getInstance().initTextViewListeners();
                    UnencryptedMessages.getInstance().initMessages(contact.getContactNumber());

                }


            }
        }catch (Exception e){
            Log.e("DEBUG_MODE","Exception");
            e.printStackTrace();
        }


    }

    private boolean phoneNumberExists(Map<String,String> contactPhoneCorrespondence, String phoneNumber ){
        if(contactPhoneCorrespondence.containsKey(phoneNumber))
            return true;
        else
            return false;
    }

    private Map<String,String> getContactCorrespondence (Context context){
        Map<String, String> namePhoneCorrespondence = new HashMap<>();

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            namePhoneCorrespondence.put(phoneNumber.replaceAll(" ","").replaceAll("-",""),name);
            namePhoneCorrespondence.put("00351"+phoneNumber.replaceAll(" ","").replaceAll("-",""),name);

        }
        phones.close();

        return namePhoneCorrespondence;
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
            Log.d("DEBUG-TIAGO","Contact #"+(i+1)+" "+listOfWhitelistedContacts.get(i).toString());
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


}
