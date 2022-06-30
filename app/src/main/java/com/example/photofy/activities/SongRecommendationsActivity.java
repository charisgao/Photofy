package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.photofy.R;
import com.example.photofy.SongAdapter;
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

public class SongRecommendationsActivity extends AppCompatActivity {

    public static final String TAG = "SongRecommendationsActivity";

    protected SongAdapter adapter;
    protected Photo photo;
    protected List<Song> recommendedSongs;

    private CardStackView csvSongs;
    private ImageButton ibAccept;
    private ImageButton ibReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_recommendations);

        Log.i(TAG, "in recommendations");

        csvSongs = findViewById(R.id.csvSongs);
        ibAccept = findViewById(R.id.ibAccept);
        ibReject = findViewById(R.id.ibReject);

        photo = getIntent().getParcelableExtra("photo");
        recommendedSongs = getIntent().getParcelableArrayListExtra("songs");
        adapter = new SongAdapter(this, recommendedSongs);

        // Set the adapter on the Card Stack View
        csvSongs.setAdapter(adapter);

        CardStackListener cardStackListener = new CardStackListener() {
            int currentPos = 0;
            boolean right = false;
            MediaPlayer mediaPlayer;

            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }

            @Override
            public void onCardSwiped(Direction direction) {
                mediaPlayer.release();
                mediaPlayer = null;
                if (direction == Direction.Right) {
                    right = true;
                    Intent i = new Intent(SongRecommendationsActivity.this, SongResultsActivity.class);
                    i.putExtra("photo", photo);
                    i.putExtra("song", recommendedSongs.get(currentPos));
                    startActivity(i);
                    finish();
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

                if (!right) {
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
            }

            @Override
            public void onCardDisappeared(View view, int position) {
            }
        };

        // Set the layout manager on the RV
        CardStackLayoutManager cardStackLayoutManager = new CardStackLayoutManager(this, cardStackListener);
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