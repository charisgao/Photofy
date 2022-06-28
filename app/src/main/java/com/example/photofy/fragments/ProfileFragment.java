package com.example.photofy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.photofy.R;
import com.example.photofy.activities.LoginActivity;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.activities.SpotifyLoginActivity;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private static final int REQUEST_CODE = 1337;
    private SharedPreferences.Editor editor;

    private Button btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), R.string.logout_error_toast, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Issue with logout", e);
                } else {
                    // TODO: figure out how to log user out from Spotify
                    // delete spotify token from shared preferences
                    editor = (getContext().getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE)).edit();
                    editor.remove("token");
                    editor.commit();

                    goLoginActivity();
                    Toast.makeText(getContext(), R.string.logout_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        ParseUser currentUser = ParseUser.getCurrentUser();
    }

    private void goLoginActivity() {
        Intent i = new Intent((MainActivity) getContext(), LoginActivity.class);
        startActivity(i);
        ((MainActivity) getContext()).finish();
    }
}