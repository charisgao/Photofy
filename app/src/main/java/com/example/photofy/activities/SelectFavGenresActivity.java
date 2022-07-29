package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.photofy.ColorToGenre;
import com.example.photofy.R;
import com.google.android.flexbox.FlexboxLayout;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SelectFavGenresActivity extends AppCompatActivity {

    private FlexboxLayout flSelect;
    private Button btnSelectSubmit;

    private List<String> favGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_fav_genres);

        flSelect = findViewById(R.id.flSelect);
        btnSelectSubmit = findViewById(R.id.btnSelectSubmit);
        favGenres = new ArrayList<>();

        for(String genre : ColorToGenre.MOOD_TO_GENRE.values()) {
            // layout to hold genre chip
            LinearLayout genreLayout = new LinearLayout(SelectFavGenresActivity.this);
            genreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected() && favGenres.size() < 3){
                        v.setSelected(true);
                        favGenres.add(genre.toLowerCase());
                        v.setBackgroundResource(R.drawable.genre_selected_bg);
                    }
                    else {
                        v.setSelected(false);
                        favGenres.remove(genre);
                        v.setBackgroundResource(R.drawable.genre_unselected_bg);
                    }
                }
            });
            genreLayout.setSelected(false);
            genreLayout.setOrientation(LinearLayout.HORIZONTAL);
            genreLayout.setBackgroundResource(R.drawable.genre_unselected_bg);
            genreLayout.setPadding(10, 10, 10, 10);

            // set margins for layout
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            genreLayout.setLayoutParams(layoutParams);

            // add textview to layout and margins
            TextView tvSelectGenre = new TextView(SelectFavGenresActivity.this);
            tvSelectGenre.setText(genre);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(10, 10, 10, 10);
            tvSelectGenre.setLayoutParams(textParams);
            genreLayout.addView(tvSelectGenre);

            flSelect.addView(genreLayout);
        }

        btnSelectSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser curr = ParseUser.getCurrentUser();
                curr.put("FavGenres", favGenres);
                curr.saveInBackground();
                goSpotifyLoginActivity();
            }
        });
    }

    private void goSpotifyLoginActivity() {
        Intent i = new Intent (this, SpotifyLoginActivity.class);
        startActivity(i);
        finish();
    }
}