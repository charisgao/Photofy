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

    public final Color blue = Color.valueOf(0,0,255); //A
    public final Color red = Color.valueOf(255,0,0); //B
    public final Color yellow = Color.valueOf(255,211,1); //C
    public final Color peach = Color.valueOf(255,192,132); //D
    public final Color pink = Color.valueOf(255,27,185); //E
    public final Color green = Color.valueOf(1,255,0); //F
    public final Color magenta = Color.valueOf(211,17,255); //G
    public final Color purple = Color.valueOf(132,132,255); //H
    public final Color lightpink = Color.valueOf(245,158,220); //I
    public final Color cyan = Color.valueOf(1,255,193); //J
    public final Color aqua = Color.valueOf(0,132,149); //K
    public final Color yellowgreen = Color.valueOf(204,204,51); //L
    public final Color brown = Color.valueOf(158,79,69); //M

    private HashMap<Color, String> colorToMood;
    private HashMap<String, String> moodToGenre;

    private Photo image;
//    private String spotifyToken;

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

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            image = bundle.getParcelable("photo");
//            spotifyToken = bundle.getString("token");
        }

        ivSongAlbumCover = view.findViewById(R.id.ivSongAlbumCover);
        tvRecommendedSong = view.findViewById(R.id.tvRecommendedSong);
        tvRecommendedArtist = view.findViewById(R.id.tvRecommendedArtist);


        // color to mood mapping source: https://www.pnas.org/doi/10.1073/pnas.1910704117#sec-1, https://www.ocf.berkeley.edu/~acowen/music.html#
        // if I have time later on, can expand to more colors through https://design-milk.com/color-coded-diaries-emotions-300-days/
        colorToMood = new HashMap<Color, String>() {{
            put(blue, "amusing");
            put(red, "annoyed");
            put(yellow, "anxious");
            put(peach, "beautiful");
            put (pink, "calm");
            put(green, "dreamy");
            put(magenta, "energizing");
            put(purple, "desirous");
            put(lightpink, "indignant");
            put(cyan, "joyful");
            put(aqua, "sad");
            put(yellowgreen, "scary");
            put(brown, "triumphant");
        }};

        // separate hash map to map to genre, possible stretch goal is to use other features besides color to determine genre
        moodToGenre = new HashMap<String, String>() {{
            put("amusing", "novelty");
            put("annoyed", "synthpop"); //hyperpop
            put("anxious", "industrial");
            put("beautiful", "classical");
            put("calm", "ambient");
            put("dreamy", "soul");
            put("energizing", "dance pop");
            put("desirous", "indie pop");
            put("indignant", "rock");
            put("joyful", "r&b");
            put("sad", "sad lo-fi");
            put("scary", "horror punk");
            put("triumphant", "epicore");
        }};

        int color = Color.parseColor(image.getColor());
        Color dominantColor = Color.valueOf(Color.red(color), Color.green(color), Color.blue(color));
        Color closestColor = getClosestColor(dominantColor);
        String mood = colorToMood.get(closestColor);
        String genre = moodToGenre.get(mood);
        Log.i(TAG, genre);

        recommendationsService = new RecommendationsService(getContext(), genre);
        getRecommendations();
    }

    public Color getClosestColor(Color dominantColor) {
        HashMap<Color, Double> distances = new HashMap<>();
        for (Color color : colorToMood.keySet()) {
            double distance = (Math.sqrt(Math.pow((color.red()-dominantColor.red()), 2) + Math.pow((color.green()- dominantColor.green()), 2) + Math.pow((color.blue()- dominantColor.blue()), 2)));
            distances.put(color, distance);
        }
        Color closest = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
        Log.i(TAG, closest.toString());
        return closest;
    }

    private void getRecommendations() {
        recommendedSongs = recommendationsService.getSongs();
        for (int i = 0; i < recommendedSongs.size(); i++) {
            Log.i(TAG, recommendedSongs.get(i).getSpotifyId());
        }
//        tvRecommendedSong.setText(recommendedSongs.get(0).getSongName());
//        tvRecommendedArtist.setText(recommendedSongs.get(0).getArtist());
    }

}