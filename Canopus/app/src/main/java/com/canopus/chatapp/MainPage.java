package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.regex.Pattern;

public class MainPage extends AppCompatActivity {

    RecyclerView starsJoinedRv;
    ImageView createStarIv, joinStarIv, invitationStarIv, helpIv;

    FirebaseAuth mAuth;
    FirebaseDatabase loginDb;
    DatabaseReference usersRef;
    ValueEventListener usersRefL;

    UserModel userModel;
    String resultUID, resultUsername, cEmail, cUID, cUsername;

    String TAG = "com.canopus.tag";
    String NAME = "com.canopus.chatapp.information";
    String starsJoinedStr; // For getting sharedpreference string
    String[] starsJoinedArr, starsJoinedPasswords, starsJoinedNames; // For getting password and name

    boolean checkUserExistence = false;

    StarAdapter starAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

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
        Structure of MainPage

        >> show user's recent stars
                >> the stars user joined & the stars user created

        >> when clicked on createStar-->
                >> go to CreateStar.class

        >> when clicked on invites-->
                >> go to Invites.class

        >> when clicked on join star-->
                >> go to joinstar.class

        always check if user exists
         */

        // Initializing shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(NAME, MODE_PRIVATE);
        starsJoinedStr = sharedPreferences.getString("StarsJoined", "<NO_STARS>");

        // Checking if recentStarsStr is not empty
        if(!starsJoinedStr.equals("<NO_STARS>")) {
            // Splitting recentStarsStr into an list
            starsJoinedArr = starsJoinedStr.split(Pattern.quote("<<|||>>"));
//            <<|||>>
            starsJoinedNames = new String[starsJoinedArr.length];
            starsJoinedPasswords = new String[starsJoinedArr.length];

            // Getting star name and password from starsJoinedArr list
            for (int i = 0; i < starsJoinedArr.length; i++) {
                starsJoinedNames[i] = starsJoinedArr[i].split(Pattern.quote("<|>"))[0];
                starsJoinedPasswords[i] = starsJoinedArr[i].split(Pattern.quote("<|>"))[1];
                Log.d(TAG, starsJoinedArr[i]);
            }
        }

        // Defining views
        starsJoinedRv = findViewById(R.id.rvStarsJoined);
        createStarIv = findViewById(R.id.ivCreateStar);
        joinStarIv = findViewById(R.id.ivJoinStar);
        invitationStarIv = findViewById(R.id.ivInvitation);
        helpIv = findViewById(R.id.ivHelp3);


        // Initializing FBDb and FBAuth and FBASE and FBUser
        mAuth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        usersRef = loginDb.getReference("users");

        FirebaseUser cUser = mAuth.getCurrentUser();

        if(cUser!=null){
            // Getting email
            cEmail = cUser.getEmail();
            cEmail = FirebaseStringCorrection.Encode(cEmail);

            // Getting uid
            cUID = cUser.getUid();

            Log.d(TAG, cEmail);
            Log.d(TAG, cUID);

            // Checking if user exists and if yes then the current UID matches the result!
            checkUserExistence = true;
            usersRefL = usersRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(checkUserExistence) {
                        checkUserExistence = false;

                        if (snapshot.exists()) {
                            userModel = snapshot.getValue(UserModel.class);

                            resultUID = userModel.getUID();

                            if (!resultUID.equals(cUID)) {
                                Log.d(TAG, "UID and fetched UID do not match");
                                Toast.makeText(getApplicationContext(), "Some error occurred! Invalid profile details.", Toast.LENGTH_SHORT).show();

                                // Signing out
                                mAuth.signOut();

                                // Exiting app
                                finish();
                            } else {
                                cUsername = userModel.getUsername();
                                // Getting FCM token
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        String FCMToken = task.getResult();
                                        userModel.setFCMToken(FCMToken);
                                        usersRef.child(cEmail).setValue(userModel);
                                    }
                                    else{
                                        Log.d(TAG, "Some error occurred while getting FCM token");
                                        Toast.makeText(MainPage.this, "Some error occurred!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } else {

                            Log.d(TAG, "User doesn't exist");

                            Toast.makeText(getApplicationContext(), "Profile not found! Please re-login into the app.", Toast.LENGTH_SHORT).show();

                            // Signing out
                            mAuth.signOut();

                            // Exiting app
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error occurred while fetching UID to check email: "+error.getDetails());

                    // Detaching listener
                    if(usersRefL!=null){
                        usersRef.removeEventListener(usersRefL);
                    }

                    Toast.makeText(getApplicationContext(), "Some error occurred while getting your profile.", Toast.LENGTH_SHORT).show();

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
            Log.d(TAG, "Removed the listener");

        }
        else{
            Toast.makeText(getApplicationContext(), "Unable to get your profile. Please login again.", Toast.LENGTH_SHORT).show();

            // Signing out
            mAuth.signOut();

            // Heading to login page
            Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            finish();
        }

        if(!starsJoinedStr.equals("<NO_STARS>")) {
            starsJoinedRv.setVisibility(View.VISIBLE);

            starsJoinedRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            // Creating an object of StarAdapter class
            starAdapter = new StarAdapter(starsJoinedNames, starsJoinedPasswords, new StarAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String starName, String starPassword) {
                    Intent joinStarPage = new Intent(getApplicationContext(), JoinStarPage.class);
                    joinStarPage.putExtra("com.canopus.app.joinStarName", starName);
                    joinStarPage.putExtra("com.canopus.app.joinStarPassword", starPassword);
                    joinStarPage.putExtra("com.canopus.app.username", cUsername);
                    startActivity(joinStarPage);
                }
            });
            starsJoinedRv.setAdapter(starAdapter);
        }
        else{
            starsJoinedRv.setVisibility(View.INVISIBLE);
        }
        
        // When pressed on Create star
        createStarIv.setOnClickListener(v -> {
            checkUserExistence = false;

            Intent AddPage = new Intent(getApplicationContext(), AddStarPage.class);
            AddPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            AddPage.putExtra("com.canopus.app.username", cUsername);
            AddPage.putExtra("com.canopus.app.encodedEmail", cEmail);
            startActivity(AddPage);
            finish();
        });

        // When pressed on Join star
        joinStarIv.setOnClickListener(v ->{
            checkUserExistence = false;

            Intent JoinStarPage = new Intent(getApplicationContext(), JoinStarPage.class);
            JoinStarPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            JoinStarPage.putExtra("com.canopus.app.username", cUsername);
            JoinStarPage.putExtra("com.canopus.app.encodedEmail", cEmail);
            startActivity(JoinStarPage);
            finish();
        });

        // When pressed on Help
        helpIv.setOnClickListener(v -> {
            checkUserExistence = false;

            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.chatapp.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "MainPage");
            startActivity(HelpPage);
            finish();
        });

    }
}