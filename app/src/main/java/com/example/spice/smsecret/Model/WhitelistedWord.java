package com.example.spice.smsecret.Model;

import com.orm.SugarRecord;

/**
 * Created by spice on 6/30/17.
 */

public class WhitelistedWord extends SugarRecord{
    String word;

    public WhitelistedWord(){}

    public WhitelistedWord(String word){
        this.word = word;
    }

    public String getWord(){
        return word;
    }
}
