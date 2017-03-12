package com.example.spice.smsecret.Model;

import com.orm.SugarRecord;

/**
 * Created by spice on 3/9/17.
 */

public class WhitelistedNumber extends SugarRecord{
    String phoneNumber;

    public WhitelistedNumber(){}

    public WhitelistedNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

}


