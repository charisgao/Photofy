package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.IOException;

public class SearchDetailsActivity extends AppCompatActivity {

    public static final String TAG = "SearchDetailsActivity";

    private ImageView ivImageSearchDetails;
    private ImageView ivProfileSearchDetails;
    private TextView tvUsernameSearchDetails;
    private ImageView ivSongPictureSearchDetails;
    private TextView tvSongNameSearchDetails;
    private TextView tvSongArtistSearchDetails;
    private TextView tvSongAlbumSearchDetails;
    private ConstraintLayout clSongDetails;

    private Post post;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_details);

        ivImageSearchDetails = findViewById(R.id.ivImageSearchDetails);
        ivProfileSearchDetails = findViewById(R.id.ivProfileSearchDetails);
        tvUsernameSearchDetails = findViewById(R.id.tvUsernameSearchDetails);
        ivSongPictureSearchDetails = findViewById(R.id.ivSongPictureSearchDetails);
        tvSongNameSearchDetails = findViewById(R.id.tvSongNameSearchDetails);
        tvSongArtistSearchDetails = findViewById(R.id.tvSongArtistSearchDetails);
        tvSongAlbumSearchDetails = findViewById(R.id.tvSongAlbumSearchDetails);
        clSongDetails = findViewById(R.id.clSongDetails);

        // Extract post from bundle
        post = getIntent().getParcelableExtra("post");

        Photo photo = (Photo) post.getPhoto();
        photo.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                Glide.with(SearchDetailsActivity.this).load(photo.getImage().getUrl()).into(ivImageSearchDetails);
            }
        });
        Glide.with(SearchDetailsActivity.this).load(post.getUser().getParseFile("Profile").getUrl()).circleCrop().into(ivProfileSearchDetails);
        tvUsernameSearchDetails.setText(post.getUser().getUsername());

        Song song = (Song) post.getSong();
        song.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                Glide.with(SearchDetailsActivity.this).load(song.getAlbumCover()).into(ivSongPictureSearchDetails);
                tvSongNameSearchDetails.setText(song.getSongName());
                tvSongArtistSearchDetails.setText(song.getArtistName());
                tvSongAlbumSearchDetails.setText(song.getAlbumName());
            }
        });

        clSongDetails.setBackgroundColor(Color.parseColor("#D4" + photo.getColor().substring(1)));

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        try {
            mediaPlayer.setDataSource(song.getPreview());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "error playing song preview " + e);
        }

        ivProfileSearchDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //followUser(post.getUser());
                //unfollowUser(post.getUser());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
    }
}