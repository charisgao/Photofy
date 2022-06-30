package com.example.photofy.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photofy.R;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SongResultsActivity extends AppCompatActivity {

    public static final String TAG = "SongResultsActivity";

    private ImageView ivResultsCapturedImage;
    private ImageView ivResultsSongImage;
    private TextView tvResultsSongName;
    private TextView tvResultsSongArtist;
    private EditText etCaption;
    private Button btnPost;

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
        Song song = getIntent().getParcelableExtra("song");
        song.saveInBackground();

        Log.i(TAG, song.getSongName());
        tvResultsSongName.setText(song.getSongName());
        tvResultsSongArtist.setText(song.getArtist());

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
            }
        });
    }
}