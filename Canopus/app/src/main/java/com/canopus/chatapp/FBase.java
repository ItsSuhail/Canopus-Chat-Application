package com.canopus.chatapp;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;

public class FBase {
    private Context context;
    private DatabaseReference emailCheckRef;
    private DatabaseReference usersRef;

    public FBase(Context context, DatabaseReference emailCheckRef, DatabaseReference usersRef){
        this.context = context;
        this.emailCheckRef = emailCheckRef;
        this.usersRef = usersRef;
    }
}
