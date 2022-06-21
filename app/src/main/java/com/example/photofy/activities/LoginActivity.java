package com.example.photofy.activities;

import static com.example.photofy.activities.SpotifyLoginActivity.spotifyToken;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.photofy.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    private EditText etLoginUsername;
    private EditText etLoginPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // If Parse user is already logged in and authorized through Spotify
        if (ParseUser.getCurrentUser() != null) {
            if (spotifyToken != null) {
                goMainActivity();
            } else {
                goSpotifyLoginActivity();
            }
        }

        // Connect visual components with logic
        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etLoginUsername.getText().toString();
                String password = etLoginPassword.getText().toString();
                loginUser(username, password);
            }
        });
    }

    // Logs in user to Parse
    private void loginUser(String username, String password) {
        Log.i(TAG, "logging in user " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
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

    private void goMainActivity() {
        Intent i = new Intent (this, MainActivity.class);
        startActivity(i);
        finish();
    }
}