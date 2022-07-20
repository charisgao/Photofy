package com.example.photofy.activities;

import static com.example.photofy.PhotofyApplication.googleCredentials;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.photofy.ColorToGenre;
import com.example.photofy.DetectProperties;
import com.example.photofy.R;
import com.example.photofy.RecommendationsCallback;
import com.example.photofy.RecommendationsErrorCallback;
import com.example.photofy.RecommendationsService;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ImageResultsActivity extends AppCompatActivity {

    public static final String TAG = "ImageResultsActivity";

    private ImageView ivCapturedImage;
    private Button btnGetSongs;

    private ColorToGenre genreFinder = new ColorToGenre();

    private Photo photo;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_results);

        ivCapturedImage = findViewById(R.id.ivCapturedImage);
        btnGetSongs = findViewById(R.id.btnGetSongs);

        photo = getIntent().getParcelableExtra("photo");
        path = getIntent().getStringExtra("filePath");
        boolean fromGallery = getIntent().getBooleanExtra("gallery", false);

        Bitmap bitmap = BitmapFactory.decodeFile(path);

        if (!fromGallery) {
            // Rotate image to be portrait
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            ivCapturedImage.setImageBitmap(rotatedBitmap);
        } else {
            ivCapturedImage.setImageBitmap(bitmap);
        }

        btnGetSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoadingFragment();
                runBackground(photo, path);
            }
        });
    }

    private void runBackground(Photo photo, String path) {
        Runnable r = new Runnable() {
            RecommendationsService recommendationsService;

            @Override
            public void run() {
                DetectProperties properties = new DetectProperties(googleCredentials, getApplicationContext());
                String color = properties.findDominantColor(photo, path);

                photo.setColor(color);
                Log.i(TAG, "generated color " + color);
                photo.saveInBackground();

                String genre = genreFinder.findGenreFromColor(color);
                Log.i(TAG, genre);

                SharedPreferences sharedPreferences = (ImageResultsActivity.this).getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");

                RequestQueue queue = Volley.newRequestQueue(ImageResultsActivity.this);
                recommendationsService = new RecommendationsService(token, queue);

                StringBuilder parameter = new StringBuilder();
                List<String> favGenres = ParseUser.getCurrentUser().getList("FavGenres");

                recommendationsService.getRecommendations(genre, favGenres, new RecommendationsCallback() {
                    @Override
                    public void callback(ArrayList<Song> songs) {
                        goToRecommendationsActivity(songs);
                    }
                }, new RecommendationsErrorCallback() {
                    @Override
                    public void callback(String errorMessage) {
                        Toast.makeText(ImageResultsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        Thread runner = new Thread(r);
        runner.start();
    }

    private void goToLoadingFragment() {
        Intent i = new Intent(this, LoadingActivity.class);
        startActivity(i);
    }

    private void goToRecommendationsActivity(ArrayList<Song> songs) {
        Intent i = new Intent(this, SongRecommendationsActivity.class);
        i.putExtra("photo", photo);
        i.putParcelableArrayListExtra("songs", songs);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}