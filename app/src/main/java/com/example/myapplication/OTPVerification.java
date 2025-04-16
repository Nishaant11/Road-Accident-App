package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class OTPVerification extends AppCompatActivity {

    EditText otpbox1,otpbox2,otpbox3,otpbox4,otpbox5,otpbox6;
    TextView numberdisplay,resendotp;
    ProgressBar otporogressbar;
    String phonenumber;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String verificationCode,enteredotp;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    Long timeoutSeconds = 60L;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        otpbox1 = findViewById(R.id.otpbox1);
        otpbox2 = findViewById(R.id.otpbox2);
        otpbox3 = findViewById(R.id.otpbox3);
        otpbox4 = findViewById(R.id.otpbox4);
        otpbox5 = findViewById(R.id.otpbox5);
        otpbox6 = findViewById(R.id.otpbox6);
        numberdisplay = findViewById(R.id.numberdisplay);
        resendotp = findViewById(R.id.resendotp);
        otporogressbar = findViewById(R.id.otpprogressbar);
        phonenumber = getIntent().getStringExtra("mobile");
        Log.d("otp", phonenumber);
        TextView textView = findViewById(R.id.numberdisplay);
        textView.setText(String.format(
                phonenumber

        ));


        sendOTP(phonenumber, false);


        numberotpmove();

        resendotp.setOnClickListener((v) -> sendOTP(phonenumber,true));

        numberotpmove();



    }

    void sendOTP(String phonenumber, boolean isResend){
        Log.d("sendOTP", "Sending OTP for phone number: " + phonenumber);
        clearEnteredOTPField();
        startResendTimer();
        setInProgress(true);
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phonenumber)
                        .setTimeout(60L,TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Log.d("onVerificationCompleted", "Verification completed successfully.");
                                if (!enteredotp.equals(verificationCode)) {
                                    signIn(phoneAuthCredential);
                                }
                                setInProgress(false);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                if (!enteredotp.equals(verificationCode)) {
                                    Log.e("onVerificationFailed", "OTP verification failed", e);
                                    Toast.makeText(OTPVerification.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                                }
                                setInProgress(false);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                Log.d("onCodeSent", "OTP code sent successfully.");
                                verificationCode = s;
                                resendingToken = forceResendingToken;
                                Toast.makeText(OTPVerification.this, "OTP sent Successfully", Toast.LENGTH_SHORT).show();
                                setInProgress(false);
                            }

                        });

        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }
        else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }


    }

    void setInProgress(boolean inProgress){
        if (inProgress){
            otporogressbar.setVisibility(View.VISIBLE);
        }

        else {
            otporogressbar.setVisibility(View.GONE);
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential){
        Log.d("signIn", "Signing in with phone credential...");
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if (task.isSuccessful()){
                    Log.d("signIn", "Sign in successful.");
                    Intent intent = new Intent(OTPVerification.this, MainActivity.class);
                    startActivity(intent);

                }else {
                    Log.d("signIn", "Sign in failed.");
                    Toast.makeText(OTPVerification.this, "OTP Verification failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void startResendTimer(){
        Log.d("startResendTimer", "Resend timer started.");
        resendotp.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                runOnUiThread(() -> {
                    resendotp.setText("Resend OTP in "+timeoutSeconds+" seconds");
                });
                if (timeoutSeconds <= 0){
                    timeoutSeconds = 30L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendotp.setEnabled(true);
                        resendotp.setText("Resend OTP");
                    });
                }
            }
        }, 0, 1000);
    }

    private void numberotpmove() {

        otpbox1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    otpbox2.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otpbox1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpbox1.getText().toString().isEmpty()) {
                        otpbox1.setText("");
                        return true; // Consume the event
                    }
                }
                return false;
            }
        });

        otpbox2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (count > after && s.toString().trim().isEmpty()) { // Backspace is pressed and the box is empty
//                    otpbox1.requestFocus();
//                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    otpbox3.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        otpbox2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpbox2.getText().toString().isEmpty()) {
                        otpbox1.requestFocus();
                        otpbox1.setText("");
                        return true; // Consume the event
                    }
                }
                return false;
            }
        });

        otpbox3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (count > after) { // Backspace is pressed
//                    otpbox2.requestFocus();
//                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    otpbox4.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        otpbox3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpbox3.getText().toString().isEmpty()) {
                        otpbox2.requestFocus();
                        otpbox2.setText("");
                        return true; // Consume the event
                    }
                }
                return false;
            }
        });

        otpbox4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (count > after) { // Backspace is pressed
//                    otpbox3.requestFocus();
//                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    otpbox5.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        otpbox4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpbox4.getText().toString().isEmpty()) {
                        otpbox3.requestFocus();
                        otpbox3.setText("");
                        return true; // Consume the event
                    }
                }
                return false;
            }
        });

        otpbox5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (count > after) { // Backspace is pressed
//                    otpbox4.requestFocus();
//                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    otpbox6.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {



            }
        });
        otpbox5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpbox5.getText().toString().isEmpty()) {
                        otpbox4.requestFocus();
                        otpbox4.setText("");
                        return true; // Consume the event
                    }
                }
                return false;
            }
        });

        otpbox6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (count > after) { // Backspace is pressed
//                    otpbox5.requestFocus();
//                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {


                if (!s.toString().trim().isEmpty()) {
                    // Concatenate all OTP digits into enteredotp string
                    enteredotp = otpbox1.getText().toString() +
                            otpbox2.getText().toString() +
                            otpbox3.getText().toString() +
                            otpbox4.getText().toString() +
                            otpbox5.getText().toString() +
                            otpbox6.getText().toString();  // Add the OTP from otpbox6

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode,enteredotp);
                    signIn(credential);
                    setInProgress(true);
                }

            }
        });
        otpbox6.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpbox6.getText().toString().isEmpty()) {
                        otpbox5.requestFocus();
                        otpbox5.setText("");
                        return true; // Consume the event
                    }
                }
                return false;
            }
        });

    }

    void clearEnteredOTPField() {
        otpbox1.setText("");
        otpbox2.setText("");
        otpbox3.setText("");
        otpbox4.setText("");
        otpbox5.setText("");
        otpbox6.setText("");

        otpbox1.requestFocus();
    }

}