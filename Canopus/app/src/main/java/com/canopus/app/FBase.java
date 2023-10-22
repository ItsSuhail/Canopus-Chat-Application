package com.canopus.app;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
