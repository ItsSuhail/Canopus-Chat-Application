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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignUpPage extends AppCompatActivity {

    EditText emailSignupEdt, passwordSignupEdt, usernameSignupEdt;
    Button signupBtn;
    TextView loginLbl;
    ImageView helpIv;
    ProgressBar signupPb;

    String username, email, password;
    String TAG = "com.canopus.tag";
    boolean checkUserAvailability = false;


    FirebaseAuth mAuth;
    FirebaseDatabase loginDb;
    DatabaseReference usersRef, usernameAvailableRef;
    FBase fBase;

    ValueEventListener usersRefL, usernameAvailableRefL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
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


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(loginLbl.isEnabled()) {
                    checkUserAvailability = false;
                    if (usernameAvailableRefL != null) {
                        usernameAvailableRef.removeEventListener(usernameAvailableRefL);
                    }

                    // Heading to Login Page
                    Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                    LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(LoginPage);
                    finish();
                }
            }
        });

        // Getting views
        emailSignupEdt = findViewById(R.id.edtEmailSignup);
        passwordSignupEdt = findViewById(R.id.edtPasswordSignup);
        usernameSignupEdt = findViewById(R.id.edtUsernameSignup);
        signupBtn = findViewById(R.id.btnSignup);
        loginLbl = findViewById(R.id.lblLogin);
        helpIv = findViewById(R.id.ivHelp2);
        signupPb = findViewById(R.id.pbSignup);

        signupBtn.setEnabled(true);
        loginLbl.setEnabled(true);

        // Setting up progress bar
        signupPb.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.yellow1), PorterDuff.Mode.SRC_IN );



        // Initializing FBDb, FBAuth and FBASE
        mAuth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        usersRef = loginDb.getReference("users");
        usernameAvailableRef = loginDb.getReference("usernameAvailable");


        // When pressed on Signup button
        signupBtn.setOnClickListener(v -> {
            // Hiding keyboard
            hideKeyboard(SignUpPage.this);

            signupBtn.setEnabled(false);
            loginLbl.setEnabled(false);
            helpIv.setEnabled(false);

            Log.d(TAG, "Pressed on Signup Button");

            // Getting email from edittext

            Log.d(TAG, "Getting username, email and password");

            email = emailSignupEdt.getText().toString();
            password = passwordSignupEdt.getText().toString();
            username = usernameSignupEdt.getText().toString();

            Log.d(TAG, email);
            Log.d(TAG, password);
            Log.d(TAG, username);

            // Checking is email is valid
            Log.d(TAG, "Validating email address");

            if(isValidEmail(email)){
                // Checking if password is not empty
                if(!password.isEmpty() && password.length() >= 6){

                    // Checking if username is valid
                    if(FirebaseStringCorrection.IsValidName(username)) {
                        // Checking if username is available to use
                        Log.d(TAG, "Trying to creating account");

                        // Enabling progress bar
                        showSignupPb();

                        createAccount();
                    }

                    // If not
                    else{
                        Toast.makeText(this, "Don't use special characters in your username. Learn more in \"HELP\" section...", Toast.LENGTH_SHORT).show();
                    }
                }

                // If password is empty
                else{
                    Toast.makeText(this, "Error! Password must contain at least 6 characters.", Toast.LENGTH_LONG).show();
                }

            }

            // If email is not valid
            else{
                Log.d(TAG, "Invalid email address!");

                Toast.makeText(this, "Invalid Email Address! Please use a correct email address", Toast.LENGTH_LONG).show();
            }

            loginLbl.setEnabled(true);
            signupBtn.setEnabled(true);
            helpIv.setEnabled(true);
        });

        // When pressed on Login lbl
        loginLbl.setOnClickListener(v -> {
            checkUserAvailability = false;

            // Heading to Login Page
            Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            finish();
        });

        // When pressed on Help
        helpIv.setOnClickListener(v -> {
            checkUserAvailability = false;

            // Heading to help page
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.chatapp.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "SignUpPage");
            startActivity(HelpPage);
            finish();
        });

    }
    public void addNewUser(String username, String email, String UID){

        String encodedEmail = FirebaseStringCorrection.Encode(email);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                usernameAvailableRef.child(username).setValue("true");
                Log.d(TAG, "got FCM Token");
                String FCMToken = task.getResult();

                UserModel userModel = new UserModel(username, encodedEmail, UID, FCMToken);
                usersRef.child(encodedEmail).setValue(userModel);
            }
            else{
                checkUserAvailability = false;
                if(usernameAvailableRefL!=null) {
                    usernameAvailableRef.removeEventListener(usernameAvailableRefL);
                }

                Log.d(TAG, "Unable to get FCM Token");
                Toast.makeText(this, "Some error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });

//        emailCheckRef.child(email).setValue(UID);
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void createAccount(){
        checkUserAvailability = true;

        // Encoding email for Firebase DB
        email = FirebaseStringCorrection.Encode(email);

        // Checking if username exists
        usernameAvailableRefL = usernameAvailableRef.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (checkUserAvailability) {
                    checkUserAvailability = false;
                    if (!snapshot.exists()) {
                        // Decoding the email
                        email = FirebaseStringCorrection.Decode(email);

                        // Creating account
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Registration succeeded!");

                                Toast.makeText(getApplicationContext(), "Registration succeeded!", Toast.LENGTH_SHORT).show();

                                Log.d(TAG, "Adding username to Firebase");

                                // Getting current user and adding to firebase database
                                FirebaseUser cUser = mAuth.getCurrentUser();
                                String cUID = cUser.getUid();
                                addNewUser(username, email, cUID);

                                // Signing out and heading to login activity
                                mAuth.signOut();

                                Intent LoginPage = new Intent(getApplicationContext(), LoginPage.class);
                                LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(LoginPage);

                                finish();
                            }

                            // If task is not successful
                            else {
                                Log.d(TAG, "Registration failed, Authentication error: " + String.valueOf(task.getException()));

                                if (task.getException() != null) {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthWeakPasswordException e) {
                                        Toast.makeText(SignUpPage.this, "Your password is weak.", Toast.LENGTH_SHORT).show();
                                    } catch (FirebaseAuthUserCollisionException e) {
                                        Toast.makeText(SignUpPage.this, "Email is already in use!", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(SignUpPage.this, "Authentication Failed! Please check your network.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(SignUpPage.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            hideSignupPb();

                        });
                    }

                    // If snapshot exists
                    else {
                        hideSignupPb();

                        Log.d(TAG, "Username already in use.");
                        Toast.makeText(getApplicationContext(), "Your chosen username is already in use. Try another one!", Toast.LENGTH_LONG).show();

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideSignupPb();
                Log.d(TAG, String.valueOf(error.getDetails()));
                Toast.makeText(getApplicationContext(), "Some error occurred while we tried to create your account. Try again", Toast.LENGTH_LONG).show();

                removeListener();

            }
        });

        // Remove listener
        usernameAvailableRef.removeEventListener(usernameAvailableRefL);
        Log.d(TAG, "Removed on valueEventListener");

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

    public void showSignupPb(){
        signupPb.setVisibility(View.VISIBLE);
    }
    public void hideSignupPb(){
        signupPb.setVisibility(View.GONE);
    }

    public void removeListener(){
        // Removing event listener
        if(usernameAvailableRefL!=null){
            usernameAvailableRef.removeEventListener(usernameAvailableRefL);
            Log.d(TAG, "Removed on valueEventListener");
        }
    }
}