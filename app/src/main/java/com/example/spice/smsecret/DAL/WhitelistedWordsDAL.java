package com.example.spice.smsecret.DAL;

import android.util.Log;

import com.example.spice.smsecret.Model.WhitelistedNumber;
import com.example.spice.smsecret.Model.WhitelistedWord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by spice on 6/30/17.
 */

public class WhitelistedWordsDAL {
    public static boolean wordExistsInDB(String word){
        List<String> list = getAllWhitelistedWords();
        for(int i=0; i<list.size();i++){
            if(word.equals(list.get(i)))
                return true;
        }
        return false;
    }

    public static void saveWordToDB(String word){
        WhitelistedWord whitelistedWord = new WhitelistedWord(word);
        whitelistedWord.save();
    }

    public static boolean deleteFromDB(String word){
        boolean flag = false;
        WhitelistedWord whitelistedWord = new WhitelistedWord(word);
        List<WhitelistedWord> list =
                whitelistedWord.find(WhitelistedWord.class, "WORD=?",word);
        for (int i=0;i<list.size();i++){
            list.get(i).delete();
            flag = true;
        }

        return flag;
    }

    public static List<String> getAllWhitelistedWords(){
        Iterator<WhitelistedWord> it = WhitelistedWord.findAll(WhitelistedWord.class);
        List<String> listOfWhiteListedWords = new ArrayList<>();
        for(int i=0; it.hasNext(); i++){
            String word = it.next().getWord();
            if(word == null) {
                continue;
            }
            Log.d("SUGAR DEBUG","LIST WORD #"+i+" = "+word);
            listOfWhiteListedWords.add(i,word);
        }
        return listOfWhiteListedWords;
    }
}
