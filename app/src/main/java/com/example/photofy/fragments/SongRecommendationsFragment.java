package com.example.photofy.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.photofy.R;
import com.example.photofy.RecommendationsService;
import com.example.photofy.activities.SpotifyLoginActivity;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fragment that shows and displays the Spotify song associated with the image
public class SongRecommendationsFragment extends Fragment {

    public static final String TAG = "SongRecommendationsFragment";

    private ImageView ivSongAlbumCover;
    private TextView tvRecommendedSong;
    private TextView tvRecommendedArtist;

    private RecommendationsService recommendationsService;
    private List<Song> recommendedSongs;

    public SongRecommendationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_recommendations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivSongAlbumCover = view.findViewById(R.id.ivSongAlbumCover);
        tvRecommendedSong = view.findViewById(R.id.tvRecommendedSong);
        tvRecommendedArtist = view.findViewById(R.id.tvRecommendedArtist);

//        recommendedSongs = recommendationsService.getSongs();
//        for (int i = 0; i < recommendedSongs.size(); i++) {
//            Log.i(TAG, recommendedSongs.get(i).getSpotifyId());
//        }
    }

    private void getRecommendations() {
//        tvRecommendedSong.setText(recommendedSongs.get(0).getSongName());
//        tvRecommendedArtist.setText(recommendedSongs.get(0).getArtist());
    }

}