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

import java.util.regex.Pattern;

public class JoinStarPage extends AppCompatActivity {

    EditText starNameJoinEdt, starPasswordJoinEdt;
    Button joinStarBtn;
    ImageView helpIv;

    FirebaseDatabase starDb;
    DatabaseReference starRef;

    ValueEventListener starRefL;
    FBase fBase;

    String starName, starPassword, cUsername, cEmail; // For getting star name and password
    String addStarSharedPreference; // For adding value of recent star
    String starMembersAdd, starMembersEmailAdd; // For adding member in star db
    String curStarsJoined; //For getting current recent star
    String curStarMembers, curStarMembersEmail; // For getting members of the star


    String TAG = "com.canopus.tag";
    String NAME = "com.canopus.chatapp.information";
    boolean doCheckStar = true;
    boolean anyStarJoined = true;

    String[] arrStarNames;
    String[] arrTempJoinedStars;
    Intent cIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_star_page);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(joinStarBtn.isEnabled()) {
                    doCheckStar = false;
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
        Get intent info-->
                if name and pswd available-->
                        check if star exists-->
                            if yes-->
                                    check if user is a member-->
                                            start chat
                                    else-->
                                        error go boom
                            else -->
                                    error go boom
                else-->
                        pressed on joinbtn-->
                            check if star exists-->
                            if yes-->
                                    check if user is a member-->
                                        add user to member
                                        add to recent list
                                        start chat
                                    else-->
                                        error go boom
                            else -->
                                    error go boom
         */

        // Getting views
        starNameJoinEdt = findViewById(R.id.edtStarNameJoin);
        starPasswordJoinEdt = findViewById(R.id.edtStarPasswordJoin);
        joinStarBtn = findViewById(R.id.btnJoinStar);
        helpIv = findViewById(R.id.ivHelp5);

        // Initialising FirebaseDB;
        starDb = FirebaseDatabase.getInstance();
        starRef = starDb.getReference("star");

        // Initializing SharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences(NAME, MODE_PRIVATE);
        curStarsJoined = sharedPreferences.getString("StarsJoined", "");
        if(!curStarsJoined.equals("")){
            arrTempJoinedStars = curStarsJoined.split(Pattern.quote("<<|||>>")); // A<|>B<<|||>>B<|>C<<|||>>
            arrStarNames = new String[arrTempJoinedStars.length];

            for(int i=0; i< arrStarNames.length; i++){
                arrStarNames[i] = arrTempJoinedStars[i].split(Pattern.quote("<|>"))[0];
            }
        }
        else {
            anyStarJoined = false;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Getting intent
        cIntent = getIntent();

        if(cIntent.hasExtra("com.canopus.app.username") && cIntent.hasExtra("com.canopus.app.joinStarName") && cIntent.hasExtra("com.canopus.app.joinStarPassword")){
            Log.d(TAG, "star name, star password and username fetched!");

            cUsername = cIntent.getStringExtra("com.canopus.app.username");
            starName = cIntent.getStringExtra("com.canopus.app.joinStarName");
            starPassword = cIntent.getStringExtra("com.canopus.app.joinStarPassword");

            starNameJoinEdt.setText(starName);
            starPasswordJoinEdt.setText(starPassword);
            joinStarBtn.setEnabled(false);
            helpIv.setEnabled(false);

            // Checking if star exists
            Log.d(TAG, "checking if star exists");

            doCheckStar = true;
            starRefL = starRef.child(starName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckStar) {
                        doCheckStar = false;

                        // If star exists
                        if (snapshot.exists()) {

                            StarModel starModel = snapshot.getValue(StarModel.class);

                            // Checking password
                            if(starModel.getPassword().equals(starPassword)) {

                                // Checking if user is a member of the star
                                Log.d(TAG, "checking if user is a member of star!");
                                curStarMembers = starModel.getMembers();
                                if(curStarMembers.contains(cUsername)){
                                    Log.d(TAG, "User is a member!");

                                    Intent ChatPage = new Intent(getApplicationContext(), ChatPage.class);
                                    ChatPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    ChatPage.putExtra("com.canopus.app.star", starName);
                                    ChatPage.putExtra("com.canopus.app.username", cUsername);
                                    startActivity(ChatPage);
                                    finish();
                                }
                                else{
                                    Log.d(TAG, "User is not a member of the star!");
                                    Toast.makeText(getApplicationContext(), "You are not a member of this star.", Toast.LENGTH_LONG).show();

                                    // Removing star from recent stars
                                    String replaceJoinedStar = starName + "<|>" + starPassword + "<<|||>>";
                                    addStarSharedPreference = curStarsJoined.replace(replaceJoinedStar, "");
                                    if (addStarSharedPreference.isEmpty()) {
                                        editor.remove("StarsJoined");
                                        editor.apply();
                                    }
                                    else {
                                        editor.putString("StarsJoined", addStarSharedPreference);
                                        editor.apply();
                                    }

                                    // Heading to MainPage
                                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(MainPage);
                                    finish();
                                }
                            }

                            // If star password doesn't match
                            else{
                                Log.d(TAG, "Star password doesn't match with what was provided");

                                Toast.makeText(JoinStarPage.this, "STAR password doesn't match! The password might be changed. Try again.", Toast.LENGTH_LONG).show();

                                // Removing star from joined stars
                                String replaceJoinedStar = starName + "<|>" + starPassword + "<<|||>>";
                                addStarSharedPreference = curStarsJoined.replace(replaceJoinedStar, "");
                                if (addStarSharedPreference.isEmpty()) {
                                    editor.remove("StarsJoined");
                                    editor.apply();
                                }
                                else {
                                    editor.putString("StarsJoined", addStarSharedPreference);
                                    editor.apply();
                                }

                                // Heading to MainPage
                                Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(MainPage);
                                finish();
                            }
                        }

                        // If star doesn't exists
                        else{
                            Log.d(TAG, "star doesn't exists");

                            Toast.makeText(JoinStarPage.this, "No such STAR exists.", Toast.LENGTH_SHORT).show();

                            // Removing star from joined stars
                            String replaceJoinedStar = starName + "<|>" + starPassword + "<<|||>>";
                            addStarSharedPreference = curStarsJoined.replace(replaceJoinedStar, "");
                            if (addStarSharedPreference.isEmpty()) {
                                editor.remove("StarsJoined");
                                editor.apply();
                            }
                            else {
                                editor.putString("StarsJoined", addStarSharedPreference);
                                editor.apply();
                            }

                            // Heading to MainPage
                            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(MainPage);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Removing listener
                    if(starRefL!=null){
                        starRef.removeEventListener(starRefL);
                    }

                    Log.d(TAG, "Some error occurred while verifying if star existed and confirming password: "+error.getDetails());
                    Toast.makeText(getApplicationContext(), "Some error occurred while getting information about the STAR. Try again.", Toast.LENGTH_LONG).show();
                    doCheckStar = false;

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });

            // Removing listener
            starRef.removeEventListener(starRefL);

            joinStarBtn.setEnabled(true);
            helpIv.setEnabled(true);

        }

        else if(cIntent.hasExtra("com.canopus.app.username") && cIntent.hasExtra("com.canopus.app.encodedEmail")){
            cUsername = cIntent.getStringExtra("com.canopus.app.username");
            cEmail = cIntent.getStringExtra("com.canopus.app.encodedEmail");
        }

        else{
            Log.d(TAG, "Error fetching intent info");

            Toast.makeText(getApplicationContext(), "Some error occurred while getting information about your profile. Try again", Toast.LENGTH_LONG).show();

            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }

        // when pressed on join btn
        joinStarBtn.setOnClickListener(v -> {
            hideKeyboard(JoinStarPage.this);
            joinStarBtn.setEnabled(false);
            helpIv.setEnabled(false);

            // Getting star name and password
            starName = starNameJoinEdt.getText().toString();
            starPassword = starPasswordJoinEdt.getText().toString();

            doCheckStar = true;
            starRefL = starRef.child(starName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckStar) {
                        doCheckStar = false;

                        // If star exists
                        if (snapshot.exists()) {
                            Log.d(TAG, "STAR does exist!");

                            StarModel starModel = snapshot.getValue(StarModel.class);

                            // Checking star password
                            if(starModel.getPassword().equals(starPassword)) {
                                Log.d(TAG, "STAR password verified!");

                                // Checking if user is a member of the star
                                Log.d(TAG, "checking if user is a member of star!");
                                curStarMembers = starModel.getMembers();
                                curStarMembersEmail = starModel.getMembersEmail();

                                if(curStarMembers.contains(cUsername)){
                                    Log.d(TAG, "User is a member");

                                    // Adding star to recent stars
                                    if(anyStarJoined) {
                                        boolean isStarStored = false;
                                        for (String name : arrStarNames) {
                                            if (name.equals(starName)) {
                                                isStarStored = true;
                                                break;
                                            }
                                        }

                                        if(!isStarStored){
                                            addStarSharedPreference = curStarsJoined + starName + "<|>" + starPassword + "<<|||>>";
                                            Log.d(TAG, "Adding star to joined stars");
                                            Log.d(TAG, addStarSharedPreference);
                                            editor.putString("StarsJoined", addStarSharedPreference);
                                            editor.apply();
                                        }
                                    }
                                    else{
                                        addStarSharedPreference = curStarsJoined + starName + "<|>" + starPassword + "<<|||>>";
                                        Log.d(TAG, "Adding star to joined stars");
                                        Log.d(TAG, addStarSharedPreference);
                                        editor.putString("StarsJoined", addStarSharedPreference);
                                        editor.apply();
                                    }

                                    Intent ChatPage = new Intent(getApplicationContext(), ChatPage.class);
                                    ChatPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    ChatPage.putExtra("com.canopus.app.star", starName);
                                    ChatPage.putExtra("com.canopus.app.username", cUsername);
                                    startActivity(ChatPage);
                                    finish();
                                }

                                // User is not a member
                                else{
                                    Log.d(TAG, "User is not a member. Adding to member list");
                                    // Adding user as star member
                                    starMembersAdd = curStarMembers + cUsername + "<<|||>>";
                                    starModel.setMembers(starMembersAdd);
                                    starMembersEmailAdd = curStarMembersEmail + cEmail + "<<|||>>";
                                    starModel.setMembersEmail(starMembersEmailAdd);

                                    starRef.child(starName).setValue(starModel);

                                    // Adding star to recent stars
                                    if(anyStarJoined) {
                                        boolean isStarStored = false;
                                        for (String name : arrStarNames) {
                                            if (name.equals(starName)) {
                                                isStarStored = true;
                                                break;
                                            }
                                        }

                                        if(!isStarStored){
                                            addStarSharedPreference = curStarsJoined + starName + "<|>" + starPassword + "<<|||>>";
                                            Log.d(TAG, "Adding star to joined stars");
                                            Log.d(TAG, addStarSharedPreference);
                                            editor.putString("StarsJoined", addStarSharedPreference);
                                            editor.apply();
                                        }
                                    }
                                    else{
                                        addStarSharedPreference = curStarsJoined + starName + "<|>" + starPassword + "<<|||>>";
                                        Log.d(TAG, "Adding star to joined stars");
                                        Log.d(TAG, addStarSharedPreference);
                                        editor.putString("StarsJoined", addStarSharedPreference);
                                        editor.apply();
                                    }


                                    Intent ChatPage = new Intent(getApplicationContext(), ChatPage.class);
                                    ChatPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    ChatPage.putExtra("com.canopus.app.star", starName);
                                    ChatPage.putExtra("com.canopus.app.username", cUsername);
                                    startActivity(ChatPage);
                                    finish();
                                }
                            }

                            // If password doesn't match
                            else{
                                Log.d(TAG, "star password does not match");
                                Toast.makeText(getApplicationContext(), "STAR password does not match!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        // If star doesn't exists
                        else{
                            Log.d(TAG, "star doesn't exists");

                            Toast.makeText(getApplicationContext(), "No such STAR exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    doCheckStar = false;

                    // Removing listener
                    if(starRefL!=null){
                        starRef.removeEventListener(starRefL);
                    }

                    Log.d(TAG, "Some error occurred, while verifying if star existed and confirming the password: "+error.getDetails());
                    Toast.makeText(getApplicationContext(), "Some error occurred while fetching STAR details. Try again.", Toast.LENGTH_SHORT).show();

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });

            // Removing listener
            if(starRefL!=null){
                starRef.removeEventListener(starRefL);
            }

            joinStarBtn.setEnabled(true);
            helpIv.setEnabled(true);
        });

        // When pressed on Help image view
        helpIv.setOnClickListener(v -> {
            doCheckStar = false;
            hideKeyboard(JoinStarPage.this);

            // Heading to Help page
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.chatapp.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "JoinStarPage");
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