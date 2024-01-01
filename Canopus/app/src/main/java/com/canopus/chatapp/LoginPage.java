package com.canopus.chatapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginPage extends AppCompatActivity {

    EditText emailLoginEdt, passwordLoginEdt;
    ImageView helpIv;
    Button loginBtn;
    TextView signupLbl;
    ProgressBar loginPb;

    String password, email;
    String username;
    String TAG = "com.canopus.tag";

    boolean doCheck = false;

    FirebaseAuth mAuth;
    FirebaseDatabase loginDb;
    DatabaseReference emailCheckRef, usersRef;
    FBase fBase;

    ValueEventListener emailCheckRefL, usersRefL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(loginBtn.isEnabled()) {
                    doCheck = false;
                    if (usersRefL != null) {
                        usersRef.removeEventListener(usersRefL);
                    }

                    finish();
                }
            }
        });

        /*
        Structure of Login

        Login pressed-->
                    >> get email
                    >> get password
                    >> login with email and password
                    if error while login-->
                        >>error go boom!
                    else-->
                        head to main page
         */


        // Getting views
        emailLoginEdt = findViewById(R.id.edtEmailLogin);
        passwordLoginEdt = findViewById(R.id.edtPasswordLogin);
        loginBtn = findViewById(R.id.btnLogin);
        signupLbl = findViewById(R.id.lblSignup);
        helpIv = findViewById(R.id.ivHelp1);
        loginPb = findViewById(R.id.pbLogin);

        loginBtn.setEnabled(true);
        signupLbl.setEnabled(true);

        // Setting up Progress Bar
        loginPb.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.yellow1), PorterDuff.Mode.SRC_IN );

        // Initializing FBDb, FBAuth and FBASE
        mAuth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailCheckRef = loginDb.getReference("emailcheck");
        usersRef = loginDb.getReference("users");

        fBase = new FBase(getApplicationContext(), emailCheckRef, usersRef);

        // When pressed on Login Button
        loginBtn.setOnClickListener(v -> {
            // Hiding keyboard
            hideKeyboard(LoginPage.this);

            doCheck = true;
            loginBtn.setEnabled(false);
            signupLbl.setEnabled(false);
            helpIv.setEnabled(false);

            Log.d(TAG, "Pressed on Login button");

            // Getting email from edittext

            Log.d(TAG, "Getting email & password");

            email = emailLoginEdt.getText().toString();
            password = passwordLoginEdt.getText().toString();

            Log.d(TAG, "Validating email address");

            // Checking if email is valid
            if(isValidEmail(email) && !password.isEmpty()){
                Log.d(TAG, "Email address is valid.");

                // Enabling progressbar
                showLoginPb();

                // Signing in using Auth
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Login successful");

                            Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();

                            email = FirebaseStringCorrection.Encode(email);
                            usersRefL = usersRef.child(email).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(doCheck) {
                                        doCheck = false;
                                        if (snapshot.exists()) {
                                            // fetching username
                                            Log.d(TAG, "Fetching username");

                                            hideLoginPb();
                                            username = String.valueOf(snapshot.getValue());
                                            Toast.makeText(getApplicationContext(), "Welcome " + username, Toast.LENGTH_SHORT).show();

                                            // Heading to MainPage
                                            Intent MainPage = new Intent(getApplicationContext(), MainPage.class);
                                            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(MainPage);
                                            finish();
                                        }
                                        else{
                                            hideLoginPb();

                                            Log.d(TAG, "username doesn't exists. Database error.");

                                            Toast.makeText(getApplicationContext(), "Some error occurred! Invalid profile.", Toast.LENGTH_SHORT).show();

                                            // Signing out
                                            mAuth.signOut();

                                            // Exiting app
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    hideLoginPb();
                                    doCheck = false;
                                    Log.d(TAG, error.getDetails());

                                    Toast.makeText(getApplicationContext(), "Some error occurred while getting your profile. Please login again.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();

                                    // Exiting
                                    finish();
                                }
                            });

                        }
                        else{
                            hideLoginPb();
                            doCheck = false;

                            Log.d(TAG, "Login not successful, Authentication error: " + String.valueOf(task.getException()));
                            if(task.getException() != null){
                                try {
                                    throw task.getException();
                                }
                                catch(FirebaseTooManyRequestsException e){
                                    Toast.makeText(getApplicationContext(), "Too many requests from this device. The device is temporarily blocked.", Toast.LENGTH_SHORT).show();
                                }
                                catch(FirebaseAuthInvalidUserException e){
                                    Toast.makeText(getApplicationContext(), "Invalid login credentials. No such user exists.", Toast.LENGTH_SHORT).show();
                                }
                                catch(FirebaseAuthInvalidCredentialsException e){
                                    Toast.makeText(getApplicationContext(), "Invalid login credentials. Please check your email or password", Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "An error occurred. Unable to Login. Please check your network!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }
            // Email is invalid
            else{
                Log.d(TAG, "Invalid email address or empty password!");

                if(password.isEmpty()){
                    Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
                }
            }

            signupLbl.setEnabled(true);
            loginBtn.setEnabled(true);
            helpIv.setEnabled(true);
        });

        // When pressed on Signup lbl
        signupLbl.setOnClickListener(v -> {
            doCheck = false;
            hideKeyboard(LoginPage.this);

            // Heading to SignUpPage
            Intent SignUpPage = new Intent(getApplicationContext(), SignUpPage.class);
            SignUpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(SignUpPage);
            finish();
        });

        // When pressed on Help
        helpIv.setOnClickListener(v -> {
            doCheck = false;
            hideKeyboard(LoginPage.this);

            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.chatapp.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "LoginPage");
            startActivity(HelpPage);
            finish();
        });
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
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

    public void showLoginPb(){
        loginPb.setVisibility(View.VISIBLE);
    }
    public void hideLoginPb(){
        loginPb.setVisibility(View.GONE);
    }

}