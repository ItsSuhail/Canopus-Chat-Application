package com.canopus.app;

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

import java.util.regex.Pattern;

public class MainPage extends AppCompatActivity {

    RecyclerView recentStarView;
    ImageView createStar, joinStar, invites, helpM;

    FirebaseAuth auth;
    FirebaseDatabase loginDb;
    DatabaseReference emailCheckRef, usersRef;
    ValueEventListener usersRefL, emailCheckRefL;
    FBase fBase;

    boolean doCheck = true;
    boolean doCheckUser = true;
    String resultUID, resultUNAME, cEmail, cUID, cUsername;

    String recentStarsStr; // For getting sharedpreference string
    String[] recentStars, recentStarsPswd, recentStarsName; // For getting password and name

    StarAdapter ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

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
        SharedPreferences sharedPreferences = getSharedPreferences("com.canopus.app", MODE_PRIVATE);
//        SharedPreferences.Editor e = sharedPreferences.edit();
//        e.remove("recentStars").apply();
        recentStarsStr = sharedPreferences.getString("recentStars", "<NO_STARS>");

        // Checking if recentStarsStr is not empty
        if(!recentStarsStr.equals("<NO_STARS>")) {
            // Splitting recentStarsStr into an list
            recentStars = recentStarsStr.split(Pattern.quote("<<|||>>"));
//            Pattern.quote("<<|||>>"))
            recentStarsName = new String[recentStars.length];
            recentStarsPswd = new String[recentStars.length];

            // Getting star name from recentStar list
            for (int i = 0; i < recentStars.length; i++) {
                recentStarsName[i] = recentStars[i].split(Pattern.quote("<|>"))[0];
                recentStarsPswd[i] = recentStars[i].split(Pattern.quote("<|>"))[1];
                Log.d("APP_WORK", recentStars[i]);
            }
        }

        // Getting views
        recentStarView = findViewById(R.id.recentStarView);
        createStar = findViewById(R.id.createStar);
        invites = findViewById(R.id.invites);
        joinStar = findViewById(R.id.joinStar);
        helpM = findViewById(R.id.help_4);

        // Initializing FBDb and FBAuth and FBASE and FBUser
        auth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailCheckRef = loginDb.getReference("emailcheck");
        usersRef = loginDb.getReference("users");

        FirebaseUser cUser = auth.getCurrentUser();

        doCheckUser = true;
        doCheck = true;
        if(cUser!=null){
            // Getting email
            cEmail = cUser.getEmail();
            cEmail = FirebaseStringCorrection.Encode(cEmail);
            // Getting uid
            cUID = cUser.getUid();

            Log.d("APP_WORK", cEmail);
            Log.d("APP_WORK", cUID);

            // Checking if email exists and if yes then the UID matches the result!
            emailCheckRefL = emailCheckRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(doCheck) {
                        if (snapshot.exists()) {
                            resultUID = String.valueOf(snapshot.getValue());

                            if (!resultUID.equals(cUID)) {
                                Log.d("APP_WORK", "UID and result do not match");

                                Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                // Signing out
                                auth.signOut();

                                // Exiting app
                                finish();
                                doCheck = false;
                                doCheck = true;
                            }
                            else{
                                usersRefL = usersRef.child(cEmail).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            cUsername = String.valueOf(snapshot.getValue());
                                            Log.d("APP_WORK", "Got username");
                                            Log.d("APP_WORK", cUsername);
                                        }
                                        else{
                                            Log.d("APP_WORK", "username doesn't exists");

                                            Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                            // Signing out
                                            auth.signOut();

                                            // Exiting app
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d("APP_WORK", "ERROR OCCURRED, while fetching username "+error.getDetails());

                                        Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                        // Signing out
                                        auth.signOut();
                                    }
                                });
                            }

                        } else {
                            Log.d("APP_WORK", "email doesn't exists");

                            Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
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

                    Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                    // Signing out
                    auth.signOut();
                }
            });
        }

        if(!recentStarsStr.equals("<NO_STARS>")) {
            recentStarView.setVisibility(View.VISIBLE);
            
            recentStarView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            // Creating an object of staradapter class
            ad = new StarAdapter(recentStarsName, recentStarsPswd, new StarAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String starName, String starPassword) {
                    Intent joinStarPage = new Intent(getApplicationContext(), JoinStarPage.class);
                    joinStarPage.putExtra("com.canopus.app.joinStarName", starName);
                    joinStarPage.putExtra("com.canopus.app.joinStarPswd", starPassword);
                    joinStarPage.putExtra("com.canopus.app.username", cUsername);
                    startActivity(joinStarPage);
                }
            });
            recentStarView.setAdapter(ad);
        }
        else{
            recentStarView.setVisibility(View.INVISIBLE);
        }

        // When pressed on Create star
        createStar.setOnClickListener(v -> {
            Intent AddPage = new Intent(getApplicationContext(), AddStar.class);
            AddPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            AddPage.putExtra("com.canopus.app.username", cUsername);
            doCheck = false;
            doCheckUser = false;
            startActivity(AddPage);
            finish();
        });

        // When pressed on Join star
        joinStar.setOnClickListener(v ->{
            Intent JoinStarPage = new Intent(getApplicationContext(), JoinStarPage.class);
            JoinStarPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            JoinStarPage.putExtra("com.canopus.app.username", cUsername);
            doCheck = false;
            doCheckUser = false;
            startActivity(JoinStarPage);
            finish();
        });

        // When pressed on Help
        helpM.setOnClickListener(v -> {
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.app.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "MainPage");
            doCheck = false;
            doCheckUser = false;
            startActivity(HelpPage);
            finish();
        });
    }
}