package com.canopus.chatapp;

public class UserModel {
    private String username;
    private String email;
    private String UID;
    private String FCMToken;

    public UserModel(String username, String email, String UID, String FCMToken){
        this.username = username;
        this.email = email;
        this.UID = UID;
        this.FCMToken = FCMToken;
    }

    public UserModel(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getFCMToken() {
        return FCMToken;
    }

    public void setFCMToken(String FCMToken) {
        this.FCMToken = FCMToken;
    }
}
