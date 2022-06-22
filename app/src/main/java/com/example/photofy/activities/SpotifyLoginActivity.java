package com.example.photofy.activities;

import static com.example.photofy.PhotofyApplication.spotifyKey;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.photofy.R;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SpotifyLoginActivity extends AppCompatActivity {

    public static final String TAG = "SpotifyFragment";

    private Button btnLoginSpotify;

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = spotifyKey;
    private static final String REDIRECT_URI = "intent://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_login);

        btnLoginSpotify = findViewById(R.id.btnLoginSpotify);

        btnLoginSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Spotify Auth API object instance
                AuthorizationRequest.Builder builder =
                        new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

                // Empty string to generate token with all permissions
                builder.setScopes(new String[]{""});

                AuthorizationRequest request = builder.build();
                AuthorizationClient.openLoginActivity(SpotifyLoginActivity.this, REQUEST_CODE, request);
            }
        });
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
                    String spotifyToken = response.getAccessToken();
                    Toast.makeText(SpotifyLoginActivity.this, R.string.login_toast, Toast.LENGTH_SHORT).show();
                    goMainActivity(spotifyToken);
                case ERROR:
                    Log.e(TAG, "Something went wrong with Spotify authorization");
                default:
                    break;
            }
        }
    }

    private void goMainActivity(String token) {
        Intent i = new Intent (this, MainActivity.class);
        startActivity(i);
        i.putExtra("token", token);
        finish();
    }
}