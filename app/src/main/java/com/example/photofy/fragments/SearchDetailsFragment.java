package com.example.photofy.fragments;

import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.transition.AutoTransition;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.photofy.PushNotificationService;
import com.example.photofy.R;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.models.Follow;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;

public class SearchDetailsFragment extends Fragment {

    public static final String TAG = "SearchDetailsFragment";

    private ImageView ivImageSearchDetails;
    private ImageView ivProfileSearchDetails;
    private TextView tvUsernameSearchDetails;
    private ImageView ivSongPictureSearchDetails;
    private TextView tvSongNameSearchDetails;
    private TextView tvSongArtistSearchDetails;
    private TextView tvSongAlbumSearchDetails;
    private ImageView ivAdd;
    private ImageButton ibSearchDetailsClose;
    private ConstraintLayout clSongDetails;

    private Post post;
    private MediaPlayer mediaPlayer;

    public SearchDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivImageSearchDetails = view.findViewById(R.id.ivImageSearchDetails);
        ivProfileSearchDetails = view.findViewById(R.id.ivProfileSearchDetails);
        tvUsernameSearchDetails = view.findViewById(R.id.tvUsernameSearchDetails);
        ivSongPictureSearchDetails = view.findViewById(R.id.ivSongPictureSearchDetails);
        tvSongNameSearchDetails = view.findViewById(R.id.tvSongNameSearchDetails);
        tvSongArtistSearchDetails = view.findViewById(R.id.tvSongArtistSearchDetails);
        tvSongAlbumSearchDetails = view.findViewById(R.id.tvSongAlbumSearchDetails);
        ivAdd = view.findViewById(R.id.ivAdd);
        ibSearchDetailsClose = view.findViewById(R.id.ibSearchDetailsClose);
        clSongDetails = view.findViewById(R.id.clSongDetails);

        // Extract post from bundle
        post = getArguments().getParcelable("post");

        Photo photo = (Photo) post.getPhoto();
        photo.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                Glide.with(getContext()).load(photo.getImage().getUrl()).into(ivImageSearchDetails);
            }
        });
        Glide.with(getContext()).load(post.getUser().getParseFile("Profile").getUrl()).circleCrop().into(ivProfileSearchDetails);
        tvUsernameSearchDetails.setText(post.getUser().getUsername());

        Song song = (Song) post.getSong();
        song.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                Glide.with(getContext()).load(song.getAlbumCover()).into(ivSongPictureSearchDetails);
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

        List<String> following = ParseUser.getCurrentUser().getList("Following");
        if (following.contains(post.getUser().getObjectId())) {
            ivAdd.setVisibility(View.VISIBLE);
            ivAdd.setImageResource(R.drawable.ic_check);
        } else if (!post.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            ivAdd.setVisibility(View.VISIBLE);
            ivAdd.setImageResource(R.drawable.ic_add);
        } else {
            ivAdd.setVisibility(View.GONE);
        }

        ivProfileSearchDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followController(following);
            }
        });

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followController(following);
            }
        });

        tvUsernameSearchDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlankFragment blankFragment = new BlankFragment();
                ProfileFragment profileFragment = new ProfileFragment(post.getUser());
                FragmentManager manager = ((MainActivity) getContext()).getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.flSearchDetails, blankFragment).addToBackStack(null).commit();
                manager.beginTransaction().replace(R.id.flContainer, profileFragment).addToBackStack(null).commit();
//                followController(following);
            }
        });

        ibSearchDetailsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle result = new Bundle();
                FragmentManager manager = ((MainActivity) getContext()).getSupportFragmentManager();
                manager.setFragmentResult("requestKey", result);
                BlankFragment blankFragment = new BlankFragment();
                blankFragment.setExitTransition(new AutoTransition());
                manager.beginTransaction().replace(R.id.flSearchDetails, blankFragment).addToBackStack(null).commit();
            }
        });
    }

    private void followController(List<String> following) {
        if (following.contains(post.getUser().getObjectId())) {
            unfollowUser(post.getUser());
            ivAdd.setImageResource(R.drawable.ic_add);
            Toast.makeText(getContext(), "unfollowed " + post.getUser().getUsername(), Toast.LENGTH_SHORT).show();
        } else if (!post.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            followUser(post.getUser());
            ivAdd.setImageResource(R.drawable.ic_check);
            sendFollowNotification(post.getUser());
            Toast.makeText(getContext(), "followed " + post.getUser().getUsername(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.release();
    }

    private void sendFollowNotification(ParseUser userTo) {
        PushNotificationService.pushNotification(getContext(), userTo.getString("DeviceToken"), "New follower!", ParseUser.getCurrentUser().getUsername() + " followed you");
    }

    private void followUser(ParseUser user) {
        Follow follow = new Follow();
        follow.setFrom(ParseUser.getCurrentUser());
        follow.setTo(user);
        follow.saveInBackground();

        List<String> following = ParseUser.getCurrentUser().getList("Following");
        following.add(user.getObjectId());
        ParseUser.getCurrentUser().put("Following", following);
        ParseUser.getCurrentUser().saveInBackground();
    }

    private void unfollowUser(ParseUser user) {
        ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
        query.whereEqualTo(Follow.KEY_FROM, ParseUser.getCurrentUser());
        query.whereEqualTo(Follow.KEY_TO, user);

        try {
            List<Follow> followList = query.find();
            Follow item = followList.get(0);
            ParseUser userToUnfollow = item.getTo();

            List<String> following = ParseUser.getCurrentUser().getList("Following");
            following.remove(userToUnfollow.getObjectId());
            ParseUser.getCurrentUser().put("Following", following);
            ParseUser.getCurrentUser().saveInBackground();

            item.deleteInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}