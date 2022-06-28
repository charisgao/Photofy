package com.example.photofy.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.animation.AccelerateInterpolator;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.photofy.R;
import com.example.photofy.SongAdapter;
import com.example.photofy.activities.SongResultsActivity;
import com.example.photofy.models.Song;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.List;

// TODO: need to make into an activity (do not need bottom navigation bar showing)
// Fragment that shows and displays the Spotify song associated with the image
public class SongRecommendationsFragment extends Fragment {

    public static final String TAG = "SongRecommendationsFragment";

    protected SongAdapter adapter;
    protected List<Song> recommendedSongs;

    private CardStackView csvSongs;
    private ImageButton ibAccept;
    private ImageButton ibReject;

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
        ibAccept = view.findViewById(R.id.ibAccept);
        ibReject = view.findViewById(R.id.ibReject);

        recommendedSongs = getArguments().getParcelableArrayList("songs");
        adapter = new SongAdapter(getContext(), recommendedSongs);

        // Set the adapter on the Card Stack View
        csvSongs.setAdapter(adapter);

        CardStackListener cardStackListener = new CardStackListener() {
            int currentPos = 0;
            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (direction == Direction.Right) {
                    Toast.makeText(getActivity(), "Accepted", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getContext(), SongResultsActivity.class);
                    i.putExtra("song", recommendedSongs.get(currentPos));
                    startActivity(i);
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
                currentPos = position;
            }

            @Override
            public void onCardDisappeared(View view, int position) {
            }
        };

        // Set the layout manager on the RV
        CardStackLayoutManager cardStackLayoutManager = new CardStackLayoutManager(getContext(), cardStackListener);
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setTranslationInterval(4.0f);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        csvSongs.setLayoutManager(cardStackLayoutManager);

        ibAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(Duration.Normal.duration)
                        .build();
                cardStackLayoutManager.setSwipeAnimationSetting(setting);
                csvSongs.swipe();
            }
        });

        ibReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Left)
                        .setDuration(Duration.Normal.duration)
                        .build();
                cardStackLayoutManager.setSwipeAnimationSetting(setting);
                csvSongs.swipe();
            }
        });
    }
}