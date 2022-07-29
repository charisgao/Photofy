package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.parse.ParseUser;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {

            // If Parse user is already logged in
            if (ParseUser.getCurrentUser() != null) {
                startActivity(new Intent(SplashScreenActivity.this, SpotifyLoginActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
            }
            finish();
        },SPLASH_TIME);
    }
}