package com.example.photofy.activities;

import static com.example.photofy.PhotofyApplication.spotifyKey;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.photofy.R;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SpotifyLoginActivity extends AppCompatActivity {

    public static final String TAG = "SpotifyFragment";

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = spotifyKey;
    private static final String REDIRECT_URI = "intent://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_login);

        authenticateUser();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Save token in persistent storage with SharedPreferences
                    String token = response.getAccessToken();
                    Toast.makeText(SpotifyLoginActivity.this, R.string.login_toast, Toast.LENGTH_SHORT).show();
                    goMainActivity(token);
                case ERROR:
                    Log.e(TAG, "Something went wrong with Spotify authorization");
                default:
                    break;
            }
        }
    }

    public void authenticateUser() {
        // Spotify Auth API object instance
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        // Empty string to generate token with all permissions
        builder.setScopes(new String[]{""});

        AuthorizationRequest request = builder.build();

        // Opens Spotify Login screen
        AuthorizationClient.openLoginActivity(SpotifyLoginActivity.this, REQUEST_CODE, request);
    }

    private void goMainActivity(String token) {
        Intent i = new Intent (this, MainActivity.class);
        i.putExtra("token", token);
        startActivity(i);
        finish();
    }
}