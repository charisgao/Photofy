package com.example.photofy.activities;

import static com.example.photofy.PhotofyApplication.spotifyKey;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

public class SongResultsActivity extends AppCompatActivity {

    public static final String TAG = "SongResultsActivity";
    private static final String CLIENT_ID = spotifyKey;
    private static final String REDIRECT_URI = "intent://";

    private ImageView ivResultsCapturedImage;
    private ImageView ivResultsSongImage;
    private TextView tvResultsSongName;
    private TextView tvResultsSongArtist;
    private EditText etCaption;
    private Button btnPost;

    private Song song;

    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_results);

        ivResultsCapturedImage = findViewById(R.id.ivResultsCapturedImage);
        ivResultsSongImage = findViewById(R.id.ivResultsSongImage);
        tvResultsSongName = findViewById(R.id.tvResultsSongName);
        tvResultsSongArtist = findViewById(R.id.tvResultsSongArtist);
        etCaption = findViewById(R.id.etCaption);
        btnPost = findViewById(R.id.btnPost);

        Photo photo = getIntent().getParcelableExtra("photo");
        song = getIntent().getParcelableExtra("song");
        song.saveInBackground();

        Log.i(TAG, song.getSongName());

        Glide.with(this).load(photo.getImage().getUrl()).into(ivResultsCapturedImage);
        Glide.with(this).load(song.getAlbumCover()).into(ivResultsSongImage);
        tvResultsSongName.setText(song.getSongName());
        tvResultsSongArtist.setText(song.getArtistName());

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = etCaption.getText().toString();
                if (caption.isEmpty()) {
                    Toast.makeText(SongResultsActivity.this, "Caption cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(caption, currentUser, photo, song);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    public void onStart() {
        super.onStart();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        Connector.ConnectionListener connectionListener = new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                connected();
            }

            @Override
            public void onFailure(Throwable error) {
                if (error instanceof NotLoggedInException || error instanceof UserNotAuthorizedException) {
                    // trigger Spotify login
                    Intent i = new Intent(SongResultsActivity.this, SpotifyLoginActivity.class);
                    startActivity(i);
                } else if (error instanceof CouldNotFindSpotifyApp) {
                    // prompt user to download Spotify from Google Play Store
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music"));
                    startActivity(i);
                }
            }
        };

        SpotifyAppRemote.connect(this, connectionParams, connectionListener);
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + song.getSpotifyId());
    }

    private void savePost(String caption, ParseUser currentUser, Photo photo, Song song) {
        Post post = new Post();
        post.setCaption(caption);
        post.setUser(currentUser);
        post.setPhoto(photo);
        post.setSong(song);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!= null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(SongResultsActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful");
                etCaption.setText("");
                ivResultsCapturedImage.setImageResource(0);
                ivResultsSongImage.setImageResource(0);

                SpotifyAppRemote.disconnect(mSpotifyAppRemote);
                mSpotifyAppRemote = null;

                Intent i = new Intent(SongResultsActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}