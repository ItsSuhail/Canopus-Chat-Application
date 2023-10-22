package com.canopus.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.canopus.app.BuildConfig;

public class HelpPage extends AppCompatActivity {

    String pActivity;
    FirebaseAuth fAuth;
    FirebaseDatabase Db;
    DatabaseReference DbRef, UpdateLinkRef;
    ValueEventListener DbRefV, UpdateLinkRefV;

    Boolean checkUpdate = true;
    Boolean getUpdateUrl = true;

    Button logoutBtn, cUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);

        Intent cIntent = getIntent();
        if(cIntent.hasExtra("com.canopus.app.parentActivity")){
            pActivity = cIntent.getStringExtra("com.canopus.app.parentActivity");
        }

        getUpdateUrl = true;
        checkUpdate = true;

        fAuth = FirebaseAuth.getInstance();
        Db = FirebaseDatabase.getInstance();
        DbRef = Db.getReference("checkUpdate");
        UpdateLinkRef = Db.getReference("updateUrl");

        // Getting views
        logoutBtn = findViewById(R.id.logoutAcc);
        cUpdateBtn = findViewById(R.id.btnUpdateApp);

        // When pressed on logout
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();

                Intent LoginPage = new Intent(getApplicationContext(), Login.class);
                LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(LoginPage);
                finish();
            }
        });

        // When pressed on cUpdateBtn
        cUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForUpdate();
            }
        });
    }

    public void checkForUpdate(){

        getUpdateUrl = true;
        checkUpdate = true;
        // Getting application version
        int version;
//        try {
//            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
//            version = pInfo.versionCode;
//            Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//
//            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
//            return;
//        }
        version = BuildConfig.VERSION_CODE;
//        Toast.makeText(this, ""+version, Toast.LENGTH_SHORT).show();

        DbRefV = DbRef.child("latestVersion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(checkUpdate){
                    if(snapshot.exists()){
                        if(Integer.parseInt(snapshot.getValue().toString()) == version){
                            Toast.makeText(HelpPage.this, "No Update Found yet! You are using the latest version!", Toast.LENGTH_SHORT).show();
                            checkUpdate = false;
                            getUpdateUrl = false;
                            return;
                        }
                        else{
                            UpdateLinkRefV = UpdateLinkRef.child("latestVersionLink").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(getUpdateUrl){
                                        if(snapshot.exists()){
                                            checkUpdate = false;
                                            getUpdateUrl = false;

                                            String url = String.valueOf(snapshot.getValue());
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(url));
                                            startActivity(i);

                                            return;
                                        }
                                        else{
                                            Toast.makeText(HelpPage.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(HelpPage.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    Log.d("APP_WORK", "Got error while getting new update link: "+error.getDetails());
                                }
                            });
                        }
                    }
                    else{
                        Toast.makeText(HelpPage.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HelpPage.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                Log.d("APP_WORK", "Got error while checking new update: "+error.getDetails());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(pActivity.equals("Login")) {
            Intent goBack = new Intent(getApplicationContext(), Login.class);
            goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goBack);
        }
        else if(pActivity.equals("SignUp")){
            Intent goBack = new Intent(getApplicationContext(), SignUp.class);
            goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goBack);
        }
        else if(pActivity.equals("AddStar")){
            Intent goBack = new Intent(getApplicationContext(), MainPage.class);
            goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goBack);
        }
        else if(pActivity.equals("JoinStarPage")){
            Intent goBack = new Intent(getApplicationContext(), MainPage.class);
            goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goBack);
        }
        else if(pActivity.equals("MainPage")){
            Intent goBack = new Intent(getApplicationContext(), MainPage.class);
            goBack.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goBack);
        }
        finish();
    }
}