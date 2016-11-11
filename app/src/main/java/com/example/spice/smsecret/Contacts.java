package com.example.spice.smsecret;

/**
 * Created by spice on 31/07/16.
 */
public class Contacts {

    int contactNumber;
    String message;

    Contacts(){

    }

    Contacts(int contactNumber, String message){
        setContactNumber(contactNumber);
        setMessage(message);
    }

    Contacts(String message){
        setMessage(message);
    }

    public void setContactNumber(int contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getContactNumber() {
        return contactNumber;
    }

    public void setMessage(String newMessage) {
        this.message = newMessage;
    }

    public String getMessages(){

        return message;
    }
}
