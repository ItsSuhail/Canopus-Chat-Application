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

public class Login extends AppCompatActivity {

    EditText emailL, passwordL;
    ImageView helpL;
    Button loginBtn;
    TextView signupLbl;

    String pswd, email;
    String username; // For fecthing username
    boolean doCheck = true;

    FirebaseAuth auth;
    FirebaseDatabase loginDb;
    DatabaseReference emailCheckRef, usersRef;
    FBase fBase;

    ValueEventListener emailCheckRefL, usersRefL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        emailL = findViewById(R.id.email_login);
        passwordL = findViewById(R.id.password_login);
        loginBtn = findViewById(R.id.loginbtn);
        signupLbl = findViewById(R.id.signup_lbl);
        helpL = findViewById(R.id.help_2);

        // Initializing FBDb, FBAuth and FBASE
        auth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailCheckRef = loginDb.getReference("emailcheck");
        usersRef = loginDb.getReference("users");

        fBase = new FBase(getApplicationContext(), emailCheckRef, usersRef);

        // When pressed on LoginBtn
        loginBtn.setOnClickListener(v -> {
            doCheck = true;
            loginBtn.setEnabled(false);
            signupLbl.setEnabled(false);

            Log.d("APP_WORK", "Pressed on Loginbtn");

            // Getting email from edittext

            Log.d("APP_WORK", "Getting email & password");

            email = emailL.getText().toString();
            pswd = passwordL.getText().toString();

            Log.d("APP_WORK", "Validating email address");
            // Checking if email is valid
            if(isValidEmail(email) && !pswd.isEmpty()){
                Log.d("APP_WORK", "email address is Valid");

                // Signing in using Auth
                auth.signInWithEmailAndPassword(email, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("APP_WORK", "Login successful");

                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

                            email = FirebaseStringCorrection.Encode(email);
                            usersRefL = usersRef.child(email).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(doCheck) {
                                        doCheck = false;
                                        if (snapshot.exists()) {
                                            // fetching username
                                            Log.d("APP_WORK", "Fetching username");
                                            username = String.valueOf(snapshot.getValue());
                                            Toast.makeText(Login.this, "Welcome " + username, Toast.LENGTH_SHORT).show();

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
                                    Log.d("APP_WORK", error.getDetails());

                                    Toast.makeText(Login.this, "SOME ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                }
                            });

                        }
                        else{
                            Log.d("APP_WORK", String.valueOf(task.getException()));

                            Toast.makeText(Login.this, "Please check your email and password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            // Email is invalid
            else{
                Log.d("APP_WORK", "Invalid email address!");

                if(pswd.isEmpty()){
                    Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
                }
            }

            signupLbl.setEnabled(false);
            loginBtn.setEnabled(true);
        });

        // When pressed on Signup lbl
        signupLbl.setOnClickListener(v -> {
            // Heading to SignUpPage
            Intent SignUpPage = new Intent(getApplicationContext(), SignUp.class);
            SignUpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(SignUpPage);
            doCheck = false;
            finish();
        });

        // When pressed on Help
        helpL.setOnClickListener(v -> {
            Intent HelpPage = new Intent(getApplicationContext(), com.canopus.app.HelpPage.class);
            HelpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HelpPage.putExtra("com.canopus.app.parentActivity", "Login");
            doCheck = false;
            startActivity(HelpPage);
            finish();
        });
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}