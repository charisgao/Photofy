package com.example.photofy.fragments;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.photofy.R;
import com.example.photofy.RecommendationsService;
import com.example.photofy.SongAdapter;
import com.example.photofy.activities.SongResultsActivity;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.List;

// TODO: need to make into an activity (do not need bottom navigation bar showing)
// Fragment that shows and displays the Spotify song associated with the image
public class SongRecommendationsFragment extends Fragment {

    public static final String TAG = "SongRecommendationsFragment";

    protected SongAdapter adapter;
    protected List<Song> recommendedSongs;

    private RecyclerView csvSongs;

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

        csvSongs = view.findViewById(R.id.csvSongs);

        recommendedSongs = getArguments().getParcelableArrayList("songs");
        adapter = new SongAdapter(getContext(), recommendedSongs);

        // Set the adapter on the Card Stack View
        csvSongs.setAdapter(adapter);

        CardStackListener cardStackListener = new CardStackListener() {
            boolean right = false;
            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (direction == Direction.Right) {
                    Toast.makeText(getActivity(), "Accepted", Toast.LENGTH_SHORT).show();
                } else if (direction == Direction.Left){
                    Toast.makeText(getActivity(), "Rejected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCardRewound() {

            }

            @Override
            public void onCardCanceled() {

            }

            @Override
            public void onCardAppeared(View view, int position) {

            }

            @Override
            public void onCardDisappeared(View view, int position) {
                if (right) {
                    Intent i = new Intent(getContext(), SongResultsActivity.class);
                    i.putExtra("song", recommendedSongs.get(position));
                    startActivity(i);
                }
            }
        };

        // Set the layout manager on the RV
        CardStackLayoutManager cardStackLayoutManager = new CardStackLayoutManager(getContext(), cardStackListener);
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setTranslationInterval(4.0f);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        csvSongs.setLayoutManager(cardStackLayoutManager);
    }
}