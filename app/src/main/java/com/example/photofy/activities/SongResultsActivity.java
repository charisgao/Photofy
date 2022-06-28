package com.example.photofy.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SongResultsActivity extends AppCompatActivity {

    public static final String TAG = "SongResultsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, getIntent().getParcelableExtra("song").toString());

    }
}