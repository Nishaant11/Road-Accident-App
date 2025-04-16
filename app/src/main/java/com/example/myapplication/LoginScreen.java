package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;


public class LoginScreen extends AppCompatActivity {

    LinearLayout login;
    CountryCodePicker countryCodePicker;
    EditText enterphonenumber;
    ProgressBar loginprogressbar;
    boolean showErrorOnLogin = false;
    String phoneNumber;
    LinearLayout Google;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient mgoogleSignInClient;
    int RC_SIGN_IN = 20;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        login = findViewById(R.id.login);
        enterphonenumber = findViewById(R.id.enterphonenumber);
        countryCodePicker = findViewById(R.id.countrycode);
        countryCodePicker.registerCarrierNumberEditText(enterphonenumber);
        Google = findViewById(R.id.Google); //googleAuth is Google in my case
        enterphonenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (showErrorOnLogin) {
                    showErrorOnLogin = false;
                    enterphonenumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = enterphonenumber.getText().toString().trim();
                if (validateMobile(phoneNumber)) {

                    Intent intent = new Intent(getApplicationContext(), OTPVerification.class);
                    intent.putExtra("mobile",countryCodePicker.getFullNumberWithPlus());

                    startActivity(intent);

                }

                else {
                    showErrorOnLogin = true;
                    enterphonenumber.setError("Please enter a valid 10-digit number");
                }
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        mgoogleSignInClient = GoogleSignIn.getClient(this,gso);

        Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Google sign-in process
                googleSignIn();
            }
        });


    }

    private void googleSignIn() {
        // Start the Google sign-in process
        Intent intent = mgoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void googleSignOut() {
        // Sign out of Google account
        mgoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Google sign out successful, proceed with Firebase sign out
                    auth.signOut();
                    Toast.makeText(LoginScreen.this, "Sign out successful.", Toast.LENGTH_SHORT).show();
                } else {
                    // Google sign out failed
                    Toast.makeText(LoginScreen.this, "Sign out failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            }
            catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void firebaseAuth(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            map.put("profile", user.getPhotoUrl().toString());

                            database.getReference().child("users").child(user.getUid()).setValue(map);

                            Intent intent = new Intent(LoginScreen.this,MainActivity.class);
                            startActivity(intent);
                        }

                        else {
                            Toast.makeText(LoginScreen.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        // Finish the activity when back button is pressed
        finishAffinity();
    }


    boolean validateMobile(String input) {
        return input.length() == 10 && input.matches("[0-9]+");
    }
}


