package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photofy.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    private Toolbar toolbarTop;
    private TextInputEditText etSignupEmail;
    private TextInputEditText etSignupUsername;
    private TextInputEditText etSignupPassword;
    private Button btnSignup;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Connect visual components with logic
        toolbarTop = findViewById(R.id.toolbarTop);
        etSignupEmail = findViewById(R.id.etSignUpEmail);
        etSignupUsername = findViewById(R.id.etSignUpUsername);
        etSignupPassword = findViewById(R.id.etSignUpPassword);
        btnSignup = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etSignupEmail.getText().toString();
                String username = etSignupUsername.getText().toString();
                String password = etSignupPassword.getText().toString();
                signupUser(username, password, email);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        // on back button pressed
        toolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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