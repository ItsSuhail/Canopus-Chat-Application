package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddStarPage extends AppCompatActivity {

    EditText starNameCreateEdt, starPasswordEdt;
    Button createStarBtn;
    ImageView helpIv;

    FirebaseDatabase starDb;
//    DatabaseReference starRef, starMembersRef, starChatsRef;

    DatabaseReference starRef;

    ValueEventListener starRefL;
    FBase fBase;

    String starName, starPassword, cUsername, cEmail; // For getting starname and pswd
    String addStarSharedPreference; // For adding value of star created
    String starMembers; // For adding member in star db
    String curStarsJoined; //For getting current stars joined
    boolean doCheck = false;
    String NAME = "com.canopus.chatapp.information";
    String TAG = "com.canopus.tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_star_page);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(createStarBtn.isEnabled()) {
                    doCheck = false;

                    if (starRefL != null) {
                        starRef.removeEventListener(starRefL);
                    }

                    // Heading to Main page
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            }
        });


        /*
        Structure of AddStar

        pressed on create-->
                 if star name valid and does not exists-->
                        create star
                        add member
                        add to recent star list
                  else-->
                        error go boom!
         */

        // Getting views
        starNameCreateEdt = findViewById(R.id.edtStarNameCreate);
        starPasswordEdt = findViewById(R.id.edtStarPasswordCreate);
        createStarBtn = findViewById(R.id.btnCreateStar);
        helpIv = findViewById(R.id.ivHelp4);

        // Initialising FirebaseDB;
        starDb = FirebaseDatabase.getInstance();
        starRef = starDb.getReference("star");
//        starMembersRef = starDb.getReference("starMembers");
//        starChatsRef = starDb.getReference("starChats");

        // Initializing SharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences(NAME, MODE_PRIVATE);
        curStarsJoined = sharedPreferences.getString("StarsJoined", "");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Getting intent
        Intent cIntent = getIntent();
        if(cIntent.hasExtra("com.canopus.app.username") && cIntent.hasExtra("com.canopus.app.encodedEmail")){
            Log.d(TAG, "Username fetched!");
            cUsername = cIntent.getStringExtra("com.canopus.app.username");
            cEmail = cIntent.getStringExtra("com.canopus.app.encodedEmail");
        }
        else{
            Log.d(TAG, "Error fetching username through the intent.");

            Toast.makeText(getApplicationContext(), "Some error occurred! Profile not fetched correctly.", Toast.LENGTH_SHORT).show();

            // Heading to MainPage
            doCheck = false;
            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }

        // When pressed on create star button
        createStarBtn.setOnClickListener(v -> {
            hideKeyboard(AddStarPage.this);
            createStarBtn.setEnabled(false);
            helpIv.setEnabled(false);

            Log.d(TAG, "Pressed on create star button");

            // Fetching starname and password
            starName = starNameCreateEdt.getText().toString();
            starPassword = starPasswordEdt.getText().toString();

            // Checking if star name is valid
            if(FirebaseStringCorrection.IsValidName(starName) && !starPassword.isEmpty()){

                // Checking if star name is available
                Log.d(TAG, "Star name is valid");
                Log.d(TAG, "Checking if star available");

                doCheck = true;
                starRefL = starRef.child(starName).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(doCheck) {
                            doCheck = false;

                            // Is star exists
                            if (snapshot.exists()) {
                                Log.d(TAG, "Star not available.");
                                Toast.makeText(getApplicationContext(), "Star name already in use, try using a different name.", Toast.LENGTH_LONG).show();
                            }

                            // If star doesn't exists
                            else{
                                // Adding star into DB
                                Log.d(TAG, "Creating STAR, Adding star details into database");
                                starMembers = cUsername + "<<|||>>";
                                String starChats = "Canopus<|>Feel free to chat whatever you want!" +
                                        " Make sure not to share your personal credentials, like IP-Addresses, Passwords, etc." +
                                        "<|>" +
                                        FirebaseStringCorrection.getCurTime() +
                                        "<<|||>>";

                                StarModel starModel = new StarModel(starName, starPassword, starMembers, starChats, cUsername, cEmail+"<<|||>>");
                                starRef.child(starName).setValue(starModel);

//                                // Adding user as star member
//                                Log.d(TAG, "Adding user as a star member");
//                                starMembersRef.child(starName).setValue(starMembers);
//
//                                // Adding chats info
//                                starChatsRef.child(starName).setValue();

                                // Adding star to stars joined list
                                addStarSharedPreference = curStarsJoined + starName + "<|>" + starPassword + "<<|||>>";
                                Log.d(TAG, "Adding star to joined stars shared preference");
                                Log.d(TAG, addStarSharedPreference);
                                editor.putString("StarsJoined", addStarSharedPreference);
                                editor.apply();

                                // Heading to join page
                                Intent joinStarPage = new Intent(getApplicationContext(), JoinStarPage.class);
                                joinStarPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                joinStarPage.putExtra("com.canopus.app.joinStarName", starName);
                                joinStarPage.putExtra("com.canopus.app.joinStarPassword", starPassword);
                                joinStarPage.putExtra("com.canopus.app.username", cUsername);
                                startActivity(joinStarPage);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Removing star listener
                        if(starRefL!=null) {
                            starRef.removeEventListener(starRefL);
                        }

                        Log.d(TAG, "Error occurred while checking if star was available: "+error.getDetails());
                        Toast.makeText(getApplicationContext(), "Some error occurred! Try again.", Toast.LENGTH_SHORT).show();
                        doCheck = false;

                        // Heading to MainPage
                        Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(MainPage);
                        finish();
                    }
                });

                // Removing star listener
                starRef.removeEventListener(starRefL);
            }
            else{
                Log.d(TAG, "Star name isn't valid/Star password isn't valid");

                if(starPassword.isEmpty()){
                    Toast.makeText(this, "Invalid password!", Toast.LENGTH_SHORT).show();
                }
                else if(starName.isEmpty()){
                    Toast.makeText(this, "Invalid Star name!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Do not use special characters! Learn more in \"HELP\" section", Toast.LENGTH_LONG).show();
                }
            }

            createStarBtn.setEnabled(true);
            helpIv.setEnabled(true);
        });

        // When pressed on Help
        helpIv.setOnClickListener(v -> {
            doCheck = false;
            hideKeyboard(AddStarPage.this);

            // Heading to help page
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.chatapp.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "AddStarPage");
            startActivity(HelpPage);
            finish();
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}