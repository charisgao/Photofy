package com.example.photofy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photofy.R;
import com.example.photofy.RecommendationsService;
import com.example.photofy.SongAdapter;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;

import java.util.ArrayList;
import java.util.List;

// Fragment that shows and displays the Spotify song associated with the image
public class SongRecommendationsFragment extends Fragment {

    public static final String TAG = "SongRecommendationsFragment";

    protected SongAdapter adapter;
    protected List<Song> recommendedSongs;

    private RecyclerView rvSongs;

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
        Log.i(TAG, "in recommendations");

        rvSongs = view.findViewById(R.id.rvSongs);

        recommendedSongs = getArguments().getParcelableArrayList("songs");
        adapter = new SongAdapter(getContext(), recommendedSongs);

        // Set the adapter on the RV
        rvSongs.setAdapter(adapter);

        // Set the layout manager on the RV
        CardStackLayoutManager cardStackLayoutManager = new CardStackLayoutManager(getContext());
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setTranslationInterval(4.0f);
        rvSongs.setLayoutManager(cardStackLayoutManager);
    }

}