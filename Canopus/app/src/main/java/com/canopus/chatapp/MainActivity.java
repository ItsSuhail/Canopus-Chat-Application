package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase loginDb;
    DatabaseReference emailCheckRef, usersRef;
    ValueEventListener usersRefL, emailCheckRefL;
    FBase fBase;

    String TAG = "com.canopus.tag";
    boolean doCheck = true;
    String resultUID, resultUsername, cEmail, cUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                doCheck = false;
                if(emailCheckRefL!=null){
                    emailCheckRef.removeEventListener(emailCheckRefL);
                }
                if(usersRefL!=null){
                    usersRef.removeEventListener(usersRefL);
                }

                finish();
            }
        });
        /*
        Structure of main activity

        Check if already logged in-->
                if already logged in-->
                            >> get username
                                if username not empty-->
                                    >> Head to main page
                                else-->
                                    >> error go boom!
                else -->
                               >> head to login page
         */


        doCheck = true;

        // Initializing FBDb and FBAuth and FBASE and FBUser
        mAuth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailCheckRef = loginDb.getReference("emailcheck");
        usersRef = loginDb.getReference("users");

        fBase = new FBase(getApplicationContext(), emailCheckRef, usersRef);

        FirebaseUser cUser = mAuth.getCurrentUser();

        // Checking if user is not null
        if(cUser!=null){

            // Getting email
            cEmail = cUser.getEmail();
            cEmail = FirebaseStringCorrection.Encode(cEmail);
            Log.d(TAG, cEmail);

            // Getting UID
            cUID = cUser.getUid();
            Log.d(TAG, cUID);

            // Checking email
            emailCheckRefL = emailCheckRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheck){

                        // If snapshot exists
                        if(snapshot.exists()){
                            Log.d(TAG, "email exists in the database, getting UID");

                            // Getting uid and checking if user's UID and result are equal
                            resultUID = String.valueOf(snapshot.getValue());

                            // If cUID and result is equal
                            if(resultUID.equals(cUID)){
                                Log.d(TAG, "user's UID and result were matched!");
                                // Getting username

                                usersRefL = usersRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // If username exists in DB
                                        if(snapshot.exists()){
                                            Log.d(TAG, "Got username");

                                            // Getting username from DB
                                            resultUsername = String.valueOf(snapshot.getValue());
                                            doCheck = false;

                                            Toast.makeText(MainActivity.this, "Welcome "+ resultUsername, Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "User: "+ resultUsername);

                                            // Heading to MainPage
                                            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(MainPage);
                                            finish();
                                        }

                                        // If username doesn't exists
                                        else{
                                            Log.d(TAG, "Username doesn't exists, Database Error");
                                            doCheck = false;

                                            Toast.makeText(MainActivity.this, "Some error occurred! Invalid profile.", Toast.LENGTH_SHORT).show();

                                            // Signing out
                                            mAuth.signOut();

                                            // Exiting app
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d(TAG, "Some error occurred while fetching username: "+error.getDetails());
                                        doCheck = false;

                                        Toast.makeText(MainActivity.this, "Some error occurred while getting your profile!", Toast.LENGTH_SHORT).show();

                                        // Signing out
                                        mAuth.signOut();

                                        finish();
                                    }
                                });
                            }
                            else{
                                Log.d(TAG, "UID and fetched UID do not match");

                                doCheck = false;

                                Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_SHORT).show();

                                // Signing out
                                mAuth.signOut();

                                // Exiting app
                                finish();
                            }
                        }
                        else{
                            Log.d(TAG, "Email doesn't exist");

                            doCheck = false;

                            Toast.makeText(MainActivity.this, "Profile not found! Please login again.", Toast.LENGTH_SHORT).show();
                            // Signing out
                            mAuth.signOut();

                            // Heading to login page
                            Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(LoginPage);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Some error occurred while fetching UID to check email: "+error.getDetails());

                    doCheck = false;

                    Toast.makeText(MainActivity.this, "Some error occurred while fetching your profile.", Toast.LENGTH_SHORT).show();

                    // Signing out
                    mAuth.signOut();

                    // Heading to login page
                    Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                    LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(LoginPage);
                    finish();
                }
            });
        }
        else{
            Log.d(TAG, "User is not logged in, heading to Login Page");

            Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            finish();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doCheck = false;
        if(emailCheckRefL!=null){
            emailCheckRef.removeEventListener(emailCheckRefL);
        }
        if(usersRefL!=null){
            usersRef.removeEventListener(usersRefL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        doCheck = false;
        if(emailCheckRefL!=null){
            emailCheckRef.removeEventListener(emailCheckRefL);
        }
        if(usersRefL!=null){
            usersRef.removeEventListener(usersRefL);
        }
    }
}