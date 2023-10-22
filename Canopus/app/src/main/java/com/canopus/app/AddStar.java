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

public class AddStar extends AppCompatActivity {

    EditText starName_create, starPswd_create;
    Button createbtn;
    ImageView helpAS;

    FirebaseDatabase starDb;
    DatabaseReference starRef, starMembersRef, starChatsRef;

    ValueEventListener starRefL;
    FBase fBase;

    String starNameEt, starPasswordEt, cUsername; // For getting starname and pswd
    String sharedPrefRecStar; // For adding value of recent star
    String starMemAdd; // For adding member in star db
    String curRecentStar; //For getting current recent star
    boolean doCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_star);

        /*
        Structure of AddStar

        pressed on create-->
                 if starname valid and does not exists-->
                        create star
                        add member
                        add to recent star list
                  else-->
                        error go boom!
         */

        // Getting views
        starName_create = findViewById(R.id.starName_create);
        starPswd_create = findViewById(R.id.starPswd_create);
        createbtn = findViewById(R.id.createbtn);
        helpAS = findViewById(R.id.help_5);

        // Initialising FirebaseDB;
        starDb = FirebaseDatabase.getInstance();
        starRef = starDb.getReference("star");
        starMembersRef = starDb.getReference("starMembers");
        starChatsRef = starDb.getReference("starChats");

        // Initializing SharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences("com.canopus.app", MODE_PRIVATE);
        curRecentStar = sharedPreferences.getString("recentStars", "");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Getting intent
        Intent cIntent = getIntent();
        if(cIntent.hasExtra("com.canopus.app.username")){
            Log.d("APP_WORK", "Username fetched!");
            cUsername = cIntent.getStringExtra("com.canopus.app.username");
        }
        else{
            Log.d("APP_WORK", "Error fetching username");

            Toast.makeText(getApplicationContext(), "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();

            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            doCheck = false;
            finish();
        }

        // When pressed on createbtn

        createbtn.setOnClickListener(v -> {
            doCheck = true;

            Log.d("APP_WORK", "Pressed on createbtn");

            // Fetching starname and pswd
            starNameEt = starName_create.getText().toString();
            starPasswordEt = starPswd_create.getText().toString();

            // Checking if star is valid
            if(FirebaseStringCorrection.IsValidName(starNameEt) && !starPasswordEt.isEmpty()){
                // Checking if starname is available
                Log.d("APP_WORK", "Star name is valid");
                Log.d("APP_WORK", "Checking if star available");
                starRefL = starRef.child(starNameEt).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(doCheck) {
                            doCheck = false;
                            // Is star exists
                            if (snapshot.exists()) {
                                Log.d("APP_WORK", "Star not available");
                                Toast.makeText(AddStar.this, "Star in use", Toast.LENGTH_SHORT).show();
                            }
                            // If star doesn't exists
                            else{
                                // Adding star into DB

                                Log.d("APP_WORK", "Adding star into db");
                                starRef.child(starNameEt).setValue(starPasswordEt);

                                // Adding user as star member
                                Log.d("APP_WORK", "Adding user as star member");
                                starMemAdd = cUsername + "<<|||>>";
                                starMembersRef.child(starNameEt).setValue(starMemAdd);

                                // Adding chats info
                                starChatsRef.child(starNameEt).setValue("Canopus<|>All the chats are not encrypted!" +
                                        " This is a simple chat app, Do not share any personal information, such as- ip address, password, email, phone number etc." +
                                        " Anyone inside or outside the Canopus authority might read your conversation. WHO KNOWS.<|>" +
                                        FirebaseStringCorrection.getCurTime()+"<<|||>>");

                                // Adding star to recent stars list
                                sharedPrefRecStar = curRecentStar+starNameEt + "<|>" + starPasswordEt + "<<|||>>";
                                Log.d("APP_WORK", "Adding star to recent stars");
                                Log.d("APP_WORK", sharedPrefRecStar);
                                editor.putString("recentStars", sharedPrefRecStar);
                                editor.apply();

                                // Heading to join page
                                Intent joinStarPage = new Intent(getApplicationContext(), JoinStarPage.class);
                                joinStarPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                joinStarPage.putExtra("com.canopus.app.joinStarName", starNameEt);
                                joinStarPage.putExtra("com.canopus.app.joinStarPswd", starPasswordEt);
                                joinStarPage.putExtra("com.canopus.app.username", cUsername);
                                startActivity(joinStarPage);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("APP_WORK", "ERROR, while checking if star available: "+error.getDetails());
                        Toast.makeText(AddStar.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                        doCheck = false;

                        // Heading to MainPage
                        Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(MainPage);
                        finish();
                    }
                });
            }
            else{
                Log.d("APP_WORK", "Star name isn't valid/Star password isn't valid");

                if(starPasswordEt.isEmpty()){
                    Toast.makeText(this, "Invalid password!", Toast.LENGTH_SHORT).show();
                }
                else if(starNameEt.isEmpty()){
                    Toast.makeText(this, "Invalid Star name!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Do not use special characters! Learn more in \"HELP\" section", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // When pressed on Help
        helpAS.setOnClickListener(v -> {
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.app.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "AddStar");
            doCheck = false;
            startActivity(HelpPage);
            finish();
        });
    }

    @Override
    public void onBackPressed() {

        doCheck = false;

        // Heading to MainPage
        Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainPage);
        finish();
    }
}