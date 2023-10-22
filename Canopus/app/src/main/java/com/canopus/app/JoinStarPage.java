package com.canopus.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class JoinStarPage extends AppCompatActivity {

    EditText starName_join, starPswd_join;
    Button joinbtn;
    ImageView helpJSP;

    FirebaseDatabase starDb;
    DatabaseReference starRef, starMembersRef;

    ValueEventListener starRefL, starMembersRefL;
    FBase fBase;

    String starNameEt, starPasswordEt, cUsername; // For getting star name and pswd
    String sharedPrefRecStar; // For adding value of recent star
    String starMemAdd; // For adding member in star db
    String curRecentStar; //For getting current recent star
    String curStarMembers; // For getting members of the star

    boolean doCheckStar = true;
    boolean doCheckMem = true;
    boolean recStar = true;

    String[] arrStarNames;
    String[] arrTempRecStars;
//    boolean parentAddStar = false;

    Intent cIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_star_page);

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
        starName_join = findViewById(R.id.starName_join);
        starPswd_join = findViewById(R.id.starPswd_join);
        joinbtn = findViewById(R.id.joinbtn);
        helpJSP = findViewById(R.id.help_6);

        // Initialising FirebaseDB;
        starDb = FirebaseDatabase.getInstance();
        starRef = starDb.getReference("star");
        starMembersRef = starDb.getReference("starMembers");

        // Initializing SharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences("com.canopus.app", MODE_PRIVATE);
        curRecentStar = sharedPreferences.getString("recentStars", "");
        if(curRecentStar != null && !curRecentStar.equals("")){
            arrTempRecStars = curRecentStar.split(Pattern.quote("<<|||>>")); // A<|>B<<|||>>B<|>C<<|||>>
            arrStarNames = new String[arrTempRecStars.length];

            for(int i=0; i< arrStarNames.length; i++){
                arrStarNames[i] = arrTempRecStars[i].split(Pattern.quote("<|>"))[0];
            }
        }
        else {
            recStar = false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Getting intent
        Intent cIntent = getIntent();
        doCheckStar = true;
        doCheckMem = true;

        if(cIntent.hasExtra("com.canopus.app.username") && cIntent.hasExtra("com.canopus.app.joinStarName") && cIntent.hasExtra("com.canopus.app.joinStarPswd")){
            Log.d("APP_WORK", "starname, starpswd and username fetched!");
            cUsername = cIntent.getStringExtra("com.canopus.app.username");
            starNameEt = cIntent.getStringExtra("com.canopus.app.joinStarName");
            starPasswordEt = cIntent.getStringExtra("com.canopus.app.joinStarPswd");
//            parentAddStar = true;

            starName_join.setText(starNameEt);
            starPswd_join.setText(starPasswordEt);
            // Checking if star exists
            Log.d("APP_WORK", "checking if star exists");

            starRefL = starRef.child(starNameEt).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckStar) {
                        doCheckStar = false;
                        // If star exists
                        if (snapshot.exists()) {
                            // Checking password
                            if(String.valueOf(snapshot.getValue()).equals(starPasswordEt)) {


                                // Checking if user is a member of the star
                                Log.d("APP_WORK", "checking if user is a member of star!");

                                starMembersRefL = starMembersRef.child(starNameEt).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (doCheckMem) {
                                            doCheckMem = false;
                                            if (snapshot.exists()) {
                                                // if user is a star member
                                                curStarMembers = String.valueOf(snapshot.getValue());
                                                if (curStarMembers.contains(cUsername)) {

                                                    Log.d("APP_WORK", "User is a member!");
                                                    Log.d("APP_WORK", curStarMembers);

                                                    Intent ChatPage = new Intent(getApplicationContext(), ChatPage.class);
                                                    ChatPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    ChatPage.putExtra("com.canopus.app.star", starNameEt);
                                                    ChatPage.putExtra("com.canopus.app.username", cUsername);
                                                    startActivity(ChatPage);
                                                } else {
                                                    Log.d("APP_WORK", "User is not a member!");
                                                    Toast.makeText(getApplicationContext(), "YOU ARE NOT A STAR MEMBER, TRY AGAIN!", Toast.LENGTH_SHORT).show();
                                                    doCheckStar = false;
                                                    doCheckMem = false;

                                                    // Heading to MainPage
                                                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(MainPage);
                                                    finish();
                                                }
                                            } else {
                                                Log.d("APP_WORK", "star doesn't exists, while checking member");

                                                Toast.makeText(JoinStarPage.this, "STAR DOESN'T EXISTS!", Toast.LENGTH_SHORT).show();

                                                doCheckStar = false;
                                                doCheckMem = false;

                                                // Removing star from recent stars
                                                String replaceRecStar = starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                                sharedPrefRecStar = curRecentStar.replace(replaceRecStar, "");
                                                if (sharedPrefRecStar.isEmpty()) {
                                                    editor.remove("recentStars");
                                                    editor.apply();
                                                } else {
                                                    editor.putString("recentStars", sharedPrefRecStar);
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
                                        Log.d("APP_WORK", "ERROR, while checking if user is a member!: " + error.getDetails());
                                        Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                        doCheckStar = false;
                                        doCheckMem = false;

                                        // Heading to MainPage
                                        Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(MainPage);
                                        finish();
                                    }
                                });
                            }
                            else{
                                Log.d("APP_WORK", "star password doesn't match");

                                Toast.makeText(JoinStarPage.this, "STAR PASSWORD DOESN'T MATCH!", Toast.LENGTH_SHORT).show();

                                doCheckStar = false;
                                doCheckMem = false;

                                // Removing star from recent stars
                                String replaceRecStar = starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                sharedPrefRecStar = curRecentStar.replace(replaceRecStar, "");
                                if (sharedPrefRecStar.isEmpty()) {
                                    editor.remove("recentStars");
                                    editor.apply();
                                } else {
                                    editor.putString("recentStars", sharedPrefRecStar);
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
                            Log.d("APP_WORK", "star doesn't exists");

                            Toast.makeText(JoinStarPage.this, "STAR DOESN'T EXISTS!", Toast.LENGTH_SHORT).show();
                            doCheckStar = false;
                            doCheckMem = false;

                            // Removing star from recent stars
                            String replaceRecStar = starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                            sharedPrefRecStar = curRecentStar.replace(replaceRecStar, "");
                            if(sharedPrefRecStar.isEmpty()){
                                editor.remove("recentStars");
                                editor.apply();
                            }
                            else {
                                editor.putString("recentStars", sharedPrefRecStar);
                                editor.apply();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("APP_WORK", "ERROR, while checking star's pswd!: "+error.getDetails());
                    Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                    doCheckStar = false;
                    doCheckMem = false;

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });

        }
        else if(cIntent.hasExtra("com.canopus.app.username")){
            cUsername = cIntent.getStringExtra("com.canopus.app.username");
        }
        else{
            Log.d("APP_WORK", "Error fetching intent info");

            Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();

            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }

        // when pressed on join btn
        joinbtn.setOnClickListener(v -> {
            doCheckStar = true;
            doCheckMem = true;
//            parentAddStar = false;

            // Getting starname and pswd
            starNameEt = starName_join.getText().toString();
            starPasswordEt = starPswd_join.getText().toString();

            starRefL = starRef.child(starNameEt).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheckStar) {
                        doCheckStar = false;
                        // If star exists
                        if (snapshot.exists()) {
                            // Checking star password
                            if(String.valueOf(snapshot.getValue()).equals(starPasswordEt)) {
                                // Checking if user is a member of the star
                                Log.d("APP_WORK", "checking if user is a member of star!");

                                starMembersRefL = starMembersRef.child(starNameEt).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (doCheckMem) {
                                            doCheckMem = false;
                                            if (snapshot.exists()) {
                                                // if user is a star member
                                                curStarMembers = String.valueOf(snapshot.getValue());
                                                if (curStarMembers.contains(cUsername)) {
                                                    doCheckStar = false;
                                                    doCheckMem = false;

                                                    Log.d("APP_WORK", "same username exists!");
                                                    Log.d("APP_WORK", curStarMembers);

                                                    // Adding star to recent stars
                                                    if(recStar) {
                                                        for (String e : arrStarNames) {
                                                            if (!e.equals(starNameEt)) {
                                                                sharedPrefRecStar = curRecentStar + starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                                                Log.d("APP_WORK", "Adding star to recent stars");
                                                                Log.d("APP_WORK", sharedPrefRecStar);
                                                                editor.putString("recentStars", sharedPrefRecStar);
                                                                editor.apply();
                                                            }
                                                        }
                                                    }
                                                    else{
                                                        sharedPrefRecStar = curRecentStar + starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                                        Log.d("APP_WORK", "Adding star to recent stars");
                                                        Log.d("APP_WORK", sharedPrefRecStar);
                                                        editor.putString("recentStars", sharedPrefRecStar);
                                                        editor.apply();
                                                    }

                                                    Intent ChatPage = new Intent(getApplicationContext(), ChatPage.class);
                                                    ChatPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    ChatPage.putExtra("com.canopus.app.star", starNameEt);
                                                    ChatPage.putExtra("com.canopus.app.username", cUsername);
                                                    startActivity(ChatPage);
                                                } else {
                                                    Log.d("APP_WORK", "User is not a member! Adding to member");
                                                    doCheckStar = false;
                                                    doCheckMem = false;

                                                    // Adding user as star member
                                                    Log.d("APP_WORK", "Adding user as star member");
                                                    starMemAdd = curStarMembers + cUsername + "<<|||>>";
                                                    starMembersRef.child(starNameEt).setValue(starMemAdd);

                                                    // Adding star to recent stars
                                                    if(recStar) {
                                                        for (String e : arrStarNames) {
                                                            if (!e.equals(starNameEt)) {
                                                                sharedPrefRecStar = curRecentStar + starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                                                Log.d("APP_WORK", "Adding star to recent stars");
                                                                Log.d("APP_WORK", sharedPrefRecStar);
                                                                editor.putString("recentStars", sharedPrefRecStar);
                                                                editor.apply();
                                                            }
                                                        }
                                                    }
                                                    else{
                                                        sharedPrefRecStar = curRecentStar + starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                                        Log.d("APP_WORK", "Adding star to recent stars");
                                                        Log.d("APP_WORK", sharedPrefRecStar);
                                                        editor.putString("recentStars", sharedPrefRecStar);
                                                        editor.apply();
                                                    }

                                                    // Heading to MainPage
//                                                Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
//                                                MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                startActivity(MainPage);
//                                                finish();

                                                    Intent ChatPage = new Intent(getApplicationContext(), ChatPage.class);
                                                    ChatPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    ChatPage.putExtra("com.canopus.app.star", starNameEt);
                                                    ChatPage.putExtra("com.canopus.app.username", cUsername);
                                                    startActivity(ChatPage);
                                                }
                                            } else {
                                                Log.d("APP_WORK", "star doesn't exists, while checking member");

                                                Toast.makeText(JoinStarPage.this, "STAR DOESN'T EXISTS!", Toast.LENGTH_SHORT).show();

                                                doCheckStar = false;
                                                doCheckMem = false;

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
                                        Log.d("APP_WORK", "ERROR, while checking if user is a member!: " + error.getDetails());
                                        Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                        doCheckStar = false;
                                        doCheckMem = false;

                                        // Heading to MainPage
                                        Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(MainPage);
                                        finish();
                                    }
                                });
                            }
                            else{
                                Log.d("APP_WORK", "star password doesn't match");

                                Toast.makeText(JoinStarPage.this, "STAR PASSWORD DOESN'T MATCH!", Toast.LENGTH_SHORT).show();
                                doCheckStar = false;
                                doCheckMem = false;
                            }
                        }
                        // If star doesn't exists
                        else{
                            Log.d("APP_WORK", "star doesn't exists");

                            Toast.makeText(JoinStarPage.this, "STAR DOESN'T EXISTS!", Toast.LENGTH_SHORT).show();
                            doCheckStar = false;
                            doCheckMem = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("APP_WORK", "ERROR, while checking star's pswd!: "+error.getDetails());
                    Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                    doCheckStar = false;
                    doCheckMem = false;

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
            });
        });

        // When pressed on Help
        helpJSP.setOnClickListener(v -> {
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.app.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "JoinStarPage");
            doCheckStar = false;
            doCheckMem = false;
            startActivity(HelpPage);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent MainPage = new Intent(getApplicationContext(), com.canopus.app.MainPage.class);
        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        doCheckStar = false;
        doCheckMem = false;
        startActivity(MainPage);
        finish();
    }
}