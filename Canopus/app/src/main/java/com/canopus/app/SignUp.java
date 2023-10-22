package com.canopus.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;

public class SignUp extends AppCompatActivity {

    EditText emailS, passwordS, usernameS;
    Button signupBtn;
    TextView loginLbl;
    ImageView helpS;

    String username, email, pswd;
    boolean doCheck = true;
//    boolean doCheckUsername = true;
//    boolean doCheckEmail = true;
//
//    boolean usernameAvailable = false; // Default value of isUsernameAvailable
//    boolean emailAvailable = false; // Default value of isEmailAvailable
//
//    boolean isUsernameValFetched = false;
//    boolean isEmailValFetched = false;
//
//    boolean availableUsername = false; // Used for storing the value of isUsernameAvailable
//    boolean availableEmail = false; // Used for storing the value of isEmailAvailable

    FirebaseAuth auth;
    FirebaseDatabase loginDb;
    DatabaseReference emailCheckRef, usersRef, usernameAvailableRef;
    FBase fBase;

    ValueEventListener emailCheckRefL, usersRefL, usernameAvailableRefL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        /*
        Pressed on Signup button-->
                    >> Is email valid
                            if is valid-->
                                    >> try Signup
                                            if success-->
                                                Head to login
                                            else -->
                                                Error go boom.
                            else-->
                                error goo boom.
         */

        // Getting views
        emailS = findViewById(R.id.email_signup);
        passwordS = findViewById(R.id.password_signup);
        usernameS = findViewById(R.id.username_signup);
        signupBtn = findViewById(R.id.signupbtn);
        loginLbl = findViewById(R.id.login_lbl);
        helpS = findViewById(R.id.help_3);

        // Initializing FBDb, FBAuth and FBASE
        auth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailCheckRef = loginDb.getReference("emailcheck");
        usersRef = loginDb.getReference("users");
        usernameAvailableRef = loginDb.getReference("usernameAvailable");

        fBase = new FBase(getApplicationContext(), emailCheckRef, usersRef);

        // When pressed on SignUpbtn
        signupBtn.setOnClickListener(v -> {
            signupBtn.setEnabled(false);
            loginLbl.setEnabled(false);

            Log.d("APP_WORK", "Pressed on SignUpBtn");

            // Getting email from edittext

            Log.d("APP_WORK", "Getting username, email and password");

            email = emailS.getText().toString();
            pswd = passwordS.getText().toString();
            username = usernameS.getText().toString();

            Log.d("APP_WORK", email);
            Log.d("APP_WORK", pswd);
            Log.d("APP_WORK", username);

            // Checking is email is valid
            Log.d("APP_WORK", "Validating email address");

            if(isValidEmail(email)){
                // Checking if password is not empty
                if(!pswd.isEmpty()){
                    // Checking if username is valid
                    if(FirebaseStringCorrection.IsValidName(username)) {
                        // Checking if username is available to use
                        Log.d("APP_WORK", "Creating account");
                        createAccount();
                    }
                    // If not
                    else{
                        Toast.makeText(this, "Don't use special characters! Learn more in \"HELP\" section", Toast.LENGTH_SHORT).show();
                    }
                }
                // If pswd is empty
                else{
                    Toast.makeText(this, "Invalid password!", Toast.LENGTH_SHORT).show();
                }

            }
            // If email is not valid
            else{
                Log.d("APP_WORK", "Invalid email address!");

                Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
            }

            loginLbl.setEnabled(true);
            signupBtn.setEnabled(true);
        });

        // When pressed on Login lbl
        loginLbl.setOnClickListener(v -> {
            // Heading to SignUpPage
            Intent LoginPage = new Intent(getApplicationContext(), Login.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            doCheck = false;
            finish();
        });

        // When pressed on Help
        helpS.setOnClickListener(v -> {
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.app.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "SignUp");
            doCheck = false;
            startActivity(HelpPage);
            finish();
        });
    }

    public void addNewUser(String username, String email, String UID){
        email = FirebaseStringCorrection.Encode(email);
        usernameAvailableRef.child(username).setValue("true");
        emailCheckRef.child(email).setValue(UID);
        usersRef.child(email).setValue(username);
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void createAccount(){
        // Encoding email for FBDB
        email = FirebaseStringCorrection.Encode(email);

        // Checking if username exists
        usernameAvailableRefL = usernameAvailableRef.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(doCheck) {
                    doCheck = false;

                    if (!snapshot.exists()) {

                        // Decoding the email
                        email = FirebaseStringCorrection.Decode(email);

                        // Creating account
                        auth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("APP_WORK", "Registration succeeded!");

                                    Toast.makeText(SignUp.this, "Registration succeeded!", Toast.LENGTH_SHORT).show();

                                    Log.d("APP_WORK", "Adding username to Firebase");

                                    FirebaseUser cUser = auth.getCurrentUser();
                                    String cUID = cUser.getUid();
                                    addNewUser(username, email, cUID);

                                    // Signing out and heading to login activity
                                    auth.signOut();

                                    Intent LoginPage = new Intent(getApplicationContext(), Login.class);
                                    LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(LoginPage);

                                    finish();
                                } else {
                                    Log.d("APP_WORK", String.valueOf(task.getException()));
                                    Toast.makeText(SignUp.this, "Please check your email and password!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    } else {
                        Toast.makeText(SignUp.this, "Username in use!", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                Log.d("APP_WORK", String.valueOf(error.getDetails()));
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Heading to SignUpPage
        Intent LoginPage = new Intent(getApplicationContext(), Login.class);
        LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginPage);
        doCheck = false;
        finish();
    }
}
/*

    public boolean isEmailAvailable(String email){
        int waitTime = 0;

        email = FirebaseStringCorrection.Encode(email);
        emailCheckRefL = emailCheckRef.child(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(doCheckEmail) {
                    emailAvailable = !snapshot.exists();
                    Log.d("APP_WORK", "Got the result for checking email");
                    isEmailValFetched = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                Log.d("APP_WORK", String.valueOf(error.getDetails()));
            }
        });

        while(!isEmailValFetched){
            waitTime++;
        }

        return emailAvailable;
    }

    public boolean isUsernameAvailable(String username){

        usernameAvailableRefL = usernameAvailableRef.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(doCheckUsername) {
                    usernameAvailable = !snapshot.exists();
                    Log.d("APP_WORK", "Got the result for checking username");
                    isUsernameValFetched = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                Log.d("APP_WORK", String.valueOf(error.getDetails()));
            }
        });

        return usernameAvailable;
    }
 */