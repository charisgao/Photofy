package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.photofy.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    private EditText etSignupEmail;
    private EditText etSignupUsername;
    private EditText etSignupPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Connect visual components with logic
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupUsername = findViewById(R.id.etLoginUsername);
        etSignupPassword = findViewById(R.id.etLoginPassword);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etSignupEmail.getText().toString();
                String username = etSignupUsername.getText().toString();
                String password = etSignupPassword.getText().toString();
                signupUser(username, password, email);
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