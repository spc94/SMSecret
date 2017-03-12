package com.example.spice.smsecret;

/**
 * Created by spice on 31/07/16.
 */
public class Contacts {

    int contactNumber;
    int visited;
    String message;

    Contacts(){

    }

    Contacts(int contactNumber, String message, int visited){
        setContactNumber(contactNumber);
        setMessage(message);
        setVisited(visited);
    }

    Contacts(String message){
        setMessage(message);
    }


    public void setContactNumber(int contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getVisited() {
        return visited;
    }

    public void setVisited(int visited) {
        this.visited = visited;
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
