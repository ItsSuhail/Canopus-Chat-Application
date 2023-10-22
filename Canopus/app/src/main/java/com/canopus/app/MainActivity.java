package com.canopus.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase loginDb;
    DatabaseReference emailCheckRef, usersRef;
    ValueEventListener usersRefL, emailCheckRefL;
    FBase fBase;

    boolean doCheck = true;
    String resultUID, resultUNAME, cEmail, cUID;

    @Override
    protected void onStop() {
        super.onStop();
        doCheck = false;
        if(usersRefL!=null){
            usersRef.removeEventListener(usersRefL);
        }
        if(emailCheckRefL!=null){
            emailCheckRef.removeEventListener(emailCheckRefL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doCheck = false;
        if(usersRefL!=null){
            usersRef.removeEventListener(usersRefL);
        }
        if(emailCheckRefL!=null){
            emailCheckRef.removeEventListener(emailCheckRefL);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        auth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailCheckRef = loginDb.getReference("emailcheck");
        usersRef = loginDb.getReference("users");

        fBase = new FBase(getApplicationContext(), emailCheckRef, usersRef);

        FirebaseUser cUser = auth.getCurrentUser();

        // Checking if user is not null
        if(cUser!=null){
            // Getting email

            cEmail = cUser.getEmail();
            cEmail = FirebaseStringCorrection.Encode(cEmail);
            Log.d("APP_WORK", cEmail);

            // Getting UID

            cUID = cUser.getUid();
            Log.d("APP_WORK", cUID);

            // Checking email

            emailCheckRefL = emailCheckRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheck){
                        // If snapshot exists

                        if(snapshot.exists()){
                            Log.d("APP_WORK", "email exists");

                            // Getting uid and checking if user's UID and result are equal
                            resultUID = String.valueOf(snapshot.getValue());

                            // If cUID and result is equal
                            if(resultUID.equals(cUID)){
                                Log.d("APP_WORK", "user's UID and result were matched!");
                                // Getting username

                                usersRefL = usersRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // If username exists in DB
                                        if(snapshot.exists()){
                                            Log.d("APP_WORK", "Got username");

                                            // Getting username from DB
                                            resultUNAME = String.valueOf(snapshot.getValue());
                                            doCheck = false;
                                            Toast.makeText(MainActivity.this, "Welcome "+resultUNAME, Toast.LENGTH_SHORT).show();

                                            Log.d("APP_WORK", resultUNAME);

                                            // Heading to MainPage
                                            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(MainPage);
                                            finish();
                                        }
                                        // If username doesn't exists
                                        else{
                                            Log.d("APP_WORK", "username doesn't exists");

                                            doCheck = false;

                                            Toast.makeText(MainActivity.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                            // Signing out
                                            auth.signOut();

                                            // Exiting app
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("APP_WORK", "ERROR OCCURRED, while fetching username: "+error.getDetails());

                                        doCheck = false;

                                        Toast.makeText(MainActivity.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                        // Signing out
                                        auth.signOut();
                                    }
                                });
                            }
                            else{
                                Log.d("APP_WORK", "UID and result do not match");

                                doCheck = false;

                                Toast.makeText(MainActivity.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                // Signing out
                                auth.signOut();

                                // Exiting app
                                finish();
                            }
                        }
                        else{
                            Log.d("APP_WORK", "email doesn't exists");

                            doCheck = false;

                            Toast.makeText(MainActivity.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                            // Signing out
                            auth.signOut();

                            // Exiting app
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("APP_WORK", "ERROR OCCURRED, while fetching UID to check email: "+error.getDetails());

                    doCheck = false;

                    Toast.makeText(MainActivity.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                    // Signing out
                    auth.signOut();
                }
            });
        }
        else{
            Log.d("APP_WORK", "User is not logged in, heading to Login Page");

            Intent LoginPage = new Intent(getApplicationContext(), Login.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            finish();
        }
    }
}