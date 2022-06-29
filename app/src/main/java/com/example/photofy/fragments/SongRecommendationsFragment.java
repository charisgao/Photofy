package com.example.photofy.fragments;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.photofy.R;
import com.example.photofy.SongAdapter;
import com.example.photofy.activities.SongResultsActivity;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.io.IOException;
import java.util.List;

// TODO: need to make into an activity (do not need bottom navigation bar showing)
// Fragment that shows and displays the Spotify song associated with the image
public class SongRecommendationsFragment extends Fragment {

    public static final String TAG = "SongRecommendationsFragment";

    protected SongAdapter adapter;
    protected Photo picture;
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

        picture = getArguments().getParcelable("picture");
        recommendedSongs = getArguments().getParcelableArrayList("songs");
        adapter = new SongAdapter(getContext(), recommendedSongs);

        // Set the adapter on the Card Stack View
        csvSongs.setAdapter(adapter);

        CardStackListener cardStackListener = new CardStackListener() {
            int currentPos = 0;
            MediaPlayer mediaPlayer;

            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }

            @Override
            public void onCardSwiped(Direction direction) {
                mediaPlayer.release();
                mediaPlayer = null;
                if (direction == Direction.Right) {
                    Intent i = new Intent(getContext(), SongResultsActivity.class);
                    i.putExtra("picture", picture);
                    i.putExtra("song", recommendedSongs.get(currentPos));
                    startActivity(i);
                }
            }

            @Override
            public void onCardRewound() {

            }

            @Override
            public void onCardCanceled() {

            }

            // TODO: next card loads too quickly so new song pops up even when swipe right
            @Override
            public void onCardAppeared(View view, int position) {
                currentPos = position;

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                String url = recommendedSongs.get(currentPos).getPreview();
                try {
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    Log.e(TAG, "error playing song preview " + e);
                }
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