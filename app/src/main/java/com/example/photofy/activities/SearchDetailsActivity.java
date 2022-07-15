package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.example.photofy.fragments.ProfileFragment;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class SearchDetailsActivity extends AppCompatActivity {

    private ImageView ivImageSearchDetails;
    private ImageView ivProfileSearchDetails;
    private TextView tvUsernameSearchDetails;
    private ImageView ivSongPictureSearchDetails;
    private TextView tvSongNameSearchDetails;
    private TextView tvSongArtistSearchDetails;
    private TextView tvSongAlbumSearchDetails;
    private Post post;

    // TODO: use preview

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

//        ivProfileSearchDetails.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goOtherProfile(post.getUser());
//            }
//        });
    }

//    private void goOtherProfile(ParseUser user) {
//        ProfileFragment otherProfileFragment = new ProfileFragment(user);
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.flContainer, otherProfileFragment).addToBackStack(null).commit();
//    }
}