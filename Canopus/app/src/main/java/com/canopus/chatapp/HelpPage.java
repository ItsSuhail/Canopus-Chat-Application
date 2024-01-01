package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HelpPage extends AppCompatActivity {

    String parentActivity;
    FirebaseAuth mAuth;
    FirebaseDatabase Db;
    DatabaseReference DbRef, UpdateLinkRef;
    ValueEventListener DbRefL, UpdateLinkRefL;

    Boolean checkUpdate = true;
    Boolean getUpdateUrl = true;

    Button logoutBtn, checkUpdateBtn;
    String TAG = "com.canopus.tag";

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);

        mContext = this;
        // What happens when pressed back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                checkUpdate = false;
                getUpdateUrl = false;
                if(DbRefL!=null){
                    DbRef.removeEventListener(DbRefL);
                }
                if(UpdateLinkRefL!=null){
                    UpdateLinkRef.removeEventListener(UpdateLinkRefL);
                }

                switch (parentActivity) {
                    case "LoginPage": {
                        Intent goBack = new Intent(getApplicationContext(), LoginPage.class);
                        goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(goBack);
                        break;
                    }
                    case "SignUpPage": {
                        Intent goBack = new Intent(getApplicationContext(), SignUpPage.class);
                        goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(goBack);
                        break;
                    }
                    case "AddStarPage":
                    case "MainPage":
                    case "JoinStarPage": {
                        Intent goBack = new Intent(getApplicationContext(), MainPage.class);
                        goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(goBack);
                        break;
                    }
                }
                finish();
            }
        });


        Intent cIntent = getIntent();
        if(cIntent.hasExtra("com.canopus.app.parentActivity")){
            parentActivity = cIntent.getStringExtra("com.canopus.app.parentActivity");
        }

        getUpdateUrl = false;
        checkUpdate = false;

        mAuth = FirebaseAuth.getInstance();
        Db = FirebaseDatabase.getInstance();
        DbRef = Db.getReference("checkUpdate");
        UpdateLinkRef = Db.getReference("updateUrl");

        // Getting views
        logoutBtn = findViewById(R.id.btnLogout);
        checkUpdateBtn = findViewById(R.id.btnCheckForUpdate);

        // When pressed on logout
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser cUser = mAuth.getCurrentUser();

                if(cUser!=null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setMessage("Are you sure you want to Logout?");
                    builder.setTitle("Logout");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Signing out
                            mAuth.signOut();

                            Toast.makeText(getApplicationContext(), "Successfully logged out from your account.", Toast.LENGTH_SHORT).show();
                            // Head to login page
                            Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(LoginPage);
                            finish();

                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        // When pressed on cUpdateBtn
        checkUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForUpdate();
            }
        });

    }

    public void checkForUpdate(){
        checkUpdateBtn.setEnabled(false);
        logoutBtn.setEnabled(false);

        getUpdateUrl = true;
        checkUpdate = true;
        // Getting application version
        int version;
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version = 3;
        }

        int finalVersion = version;
        DbRefL = DbRef.child("latestVersion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(checkUpdate){
                    if(snapshot.exists()){
                        if(Integer.parseInt(snapshot.getValue().toString()) == finalVersion){
                            Toast.makeText(HelpPage.this, "No update found yet! You are using the latest version!", Toast.LENGTH_SHORT).show();
                            checkUpdate = false;
                            getUpdateUrl = false;
                        }
                        else{
                            UpdateLinkRefL = UpdateLinkRef.child("latestVersionLink").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(getUpdateUrl){
                                        checkUpdate = false;
                                        getUpdateUrl = false;

                                        if(snapshot.exists()){
                                            String url = String.valueOf(snapshot.getValue());
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(url));
                                            startActivity(intent);
                                        }
                                        else{
                                            Log.d(TAG, "latest version link snapshot doesn't exist");
                                            Toast.makeText(HelpPage.this, "Something went wrong! Unable to get latest update link", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "Some error occurred while getting new update link: "+error.getDetails());
                                    Toast.makeText(HelpPage.this, "Some error occurred while fetching the latest update link. Try again.", Toast.LENGTH_LONG).show();

                                    checkUpdate = false;
                                    getUpdateUrl = false;
                                }
                            });
                        }
                    }

                    else{
                        checkUpdate = false;
                        getUpdateUrl = false;

                        Log.d(TAG, "No latest version is found");
                        Toast.makeText(HelpPage.this, "Some error occurred. No latest version to be found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkUpdate = false;
                getUpdateUrl = false;

                Log.d(TAG, "Got error while checking for latest version: "+error.getDetails());
                Toast.makeText(HelpPage.this, "Some error occurred while checking for newer versions. Try again", Toast.LENGTH_SHORT).show();
            }
        });

        if(UpdateLinkRefL!=null){
            UpdateLinkRef.removeEventListener(UpdateLinkRefL);
        }

        DbRef.removeEventListener(DbRefL);

        checkUpdateBtn.setEnabled(true);
        logoutBtn.setEnabled(true);

    }

}