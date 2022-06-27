package com.example.photofy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.photofy.R;
import com.example.photofy.fragments.ComposeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private Button btnLogout;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String spotifyToken = getIntent().getStringExtra("token");

        sharedPreferences = this.getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("token", spotifyToken);
        editor.apply();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnLogout = findViewById(R.id.btnLogout);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch(item.getItemId()) {
                    case R.id.action_home:
                        // TODO: update fragment
                        fragment = new ComposeFragment();
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                    case R.id.action_profile:
                        // TODO: update fragment
                        fragment = new ComposeFragment();
                    default:
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    // TODO: use AuthorizationClient clearCookies method to log out and clear all stored tokens for Spotify
    private void logout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, R.string.logout_error_toast, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Issue with logout", e);
                } else {
                    goLoginActivity();
                    Toast.makeText(MainActivity.this, R.string.logout_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        ParseUser currentUser = ParseUser.getCurrentUser();
    }

    private void goLoginActivity() {
        Intent i = new Intent (this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}