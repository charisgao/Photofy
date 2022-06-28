package com.example.photofy.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photofy.R;
import com.example.photofy.models.Song;

public class SongResultsActivity extends AppCompatActivity {

    public static final String TAG = "SongResultsActivity";

    private TextView tvResultsSongName;
    private TextView tvResultsSongArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_results);

        tvResultsSongName = findViewById(R.id.tvResultsSongName);
        tvResultsSongArtist = findViewById(R.id.tvResultsSongArtist);

        Song song = getIntent().getParcelableExtra("song");
        Log.i(TAG, song.getSongName());
        tvResultsSongName.setText(song.getSongName());
        tvResultsSongArtist.setText(song.getArtist());

    }
}