package com.example.spice.smsecret.DAL;

import android.util.Log;

import com.example.spice.smsecret.Model.WhitelistedNumber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by spice on 3/10/17.
 */

public class WhitelistedNumbersDAL {
    public static boolean contactExistsInDB(String phoneNumber){
        List<String> list = getAllWhitelistedNumbers();
        for(int i=0; i<list.size();i++){
            if(phoneNumber.equals(list.get(i)))
                return true;
        }
        return false;
    }

    public static void saveContactToDB(String phoneNumber){
        WhitelistedNumber whitelistedNumber = new WhitelistedNumber(phoneNumber);
        whitelistedNumber.save();
    }

    public static boolean deleteFromDB(String phoneNumber){
        boolean flag = false;
        WhitelistedNumber whitelistedNumber = new WhitelistedNumber(phoneNumber);
        List<WhitelistedNumber> list =
                WhitelistedNumber.find(WhitelistedNumber.class, "PHONE_NUMBER=?",phoneNumber);
        for (int i=0;i<list.size();i++){
            list.get(i).delete();
            flag = true;
        }

        return flag;
    }

    public static List<String> getAllWhitelistedNumbers(){
        Iterator<WhitelistedNumber> it = WhitelistedNumber.findAll(WhitelistedNumber.class);
        List<String> listOfWhiteListedNumbers = new ArrayList<>();
        for(int i=0; it.hasNext(); i++){
            String phoneNumber = it.next().getPhoneNumber();
            if(phoneNumber == null) {
                continue;
            }
            Log.d("SUGAR DEBUG","LIST PHONE NUMBER #"+i+" = "+phoneNumber);
            listOfWhiteListedNumbers.add(i,phoneNumber);
        }
        return listOfWhiteListedNumbers;
    }
}
