package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.annotations.concurrent.Background;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase loginDb;

    DatabaseReference usersRef;
    ValueEventListener usersRefL;

    String TAG = "com.canopus.tag";
    String resultUID, resultUsername, cEmail, cUID;

    boolean checkUserExistence = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createChannel();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                checkUserExistence = false;
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


        // Initializing FBDb and FBAuth and FBASE and FBUser
        mAuth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        usersRef = loginDb.getReference("users");

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

            // Checking email, uid and getting username
            checkUserExistence = true;
            usersRefL = usersRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(checkUserExistence) {
                        checkUserExistence = false;

                        // If snapshot exists
                        if (snapshot.exists()) {
                            Log.d(TAG, "user exists in the database, getting UID");

                            UserModel userModel = snapshot.getValue(UserModel.class);

                            // Getting uid and checking if user's UID and result are equal
                            resultUID = userModel.getUID();

                            // If cUID and result is equal
                            if (resultUID.equals(cUID)) {
                                Log.d(TAG, "user's UID and result were matched!");

                                // Getting FCM token
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        String FCMToken = task.getResult();
                                        userModel.setFCMToken(FCMToken);
                                        usersRef.child(cEmail).setValue(userModel);
                                    }
                                    else{
                                        Log.d(TAG, "Some error occurred while getting FCM token");
                                        Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                
                                resultUsername = userModel.getUsername();

                                Toast.makeText(MainActivity.this, "Welcome " + resultUsername, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Username: " + resultUsername);

                                // Heading to MainPage
                                Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(MainPage);
                                finish();

                            }
                            else {
                                Log.d(TAG, "UID and fetched UID do not match");

                                Toast.makeText(MainActivity.this, "Some error occurred!", Toast.LENGTH_SHORT).show();

                                // Signing out
                                mAuth.signOut();

                                // Exiting app
                                finish();
                            }
                        }
                        else {
                            Log.d(TAG, "User doesn't exist");

                            Toast.makeText(MainActivity.this, "Profile not found! Please login again.", Toast.LENGTH_SHORT).show();

                            // Signing out
                            mAuth.signOut();

                            // Heading to login page
                            Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(LoginPage);
                            finish();
                        }
                    }


                    // Detaching listener
                    if (usersRefL != null) {
                        usersRef.removeEventListener(usersRefL);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Some error occurred while fetching User profile: "+error.getDetails());

                    Toast.makeText(MainActivity.this, "Some error occurred while fetching your profile.", Toast.LENGTH_SHORT).show();

                    // Detaching listener
                    if(usersRefL!=null){
                        usersRef.removeEventListener(usersRefL);
                    }

                    // Signing out
                    mAuth.signOut();

                    // Heading to login page
                    Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                    LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(LoginPage);
                    finish();
                }
            });


            // Detaching listener
            usersRef.removeEventListener(usersRefL);

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

        checkUserExistence = false;
        if(usersRefL!=null){
            usersRef.removeEventListener(usersRefL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        checkUserExistence = false;
        if(usersRefL!=null){
            usersRef.removeEventListener(usersRefL);
        }
    }

    @Background
    void createChannel(){
        Uri sound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.received);
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel("message", "Message Notifications", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription("Message Notifications");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            mChannel.setSound(sound, audioAttributes);

            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

}