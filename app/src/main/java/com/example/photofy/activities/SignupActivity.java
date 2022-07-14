package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.photofy.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    private TextInputEditText etSignupEmail;
    private TextInputEditText etSignupUsername;
    private TextInputEditText etSignUpConfirmPassword;
    private TextInputEditText etSignupPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Connect visual components with logic
        etSignupEmail = findViewById(R.id.etSignUpEmail);
        etSignupUsername = findViewById(R.id.etSignUpUsername);
        etSignupPassword = findViewById(R.id.etSignUpPassword);
        etSignUpConfirmPassword = findViewById(R.id.etSignUpConfirmPassword);
        btnSignup = findViewById(R.id.btnSignUp);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etSignupEmail.getText().toString();
                String username = etSignupUsername.getText().toString();
                String password = etSignupPassword.getText().toString();
                String confirmPassword = etSignUpConfirmPassword.getText().toString();
                if (password.equals(confirmPassword)) {
                    signupUser(username, password, email);
                } else {
                    Toast.makeText(SignupActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Signs up user to Parse
    private void signupUser(String username, String password, String email) {
        Log.i(TAG, "Attempting to sign up user " + username);

        ParseUser user = new ParseUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    return;
                }
                goSpotifyLoginActivity();
            }
        });
    }

    private void goSpotifyLoginActivity() {
        Intent i = new Intent (this, SpotifyLoginActivity.class);
        startActivity(i);
        finish();
    }
}