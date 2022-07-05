package com.example.photofy.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.photofy.ProfileAdapter;
import com.example.photofy.R;
import com.example.photofy.activities.EditProfileActivity;
import com.example.photofy.activities.LoginActivity;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.models.Post;
import com.google.android.material.appbar.MaterialToolbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    protected ProfileAdapter profileAdapter;
    protected List<Post> profilePosts;

    private static final int REQUEST_CODE = 1337;
    private SharedPreferences.Editor editor;

    private MaterialToolbar tbProfile;
    private ImageView ivProfilePicture;
    private TextView tvProfileBiography;
    private Button btnEditProfile;
    private RecyclerView rvProfilePosts;

    private ParseUser user = ParseUser.getCurrentUser();

    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(ParseUser user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tbProfile = view.findViewById(R.id.tbProfile);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvProfileBiography = view.findViewById(R.id.tvProfileBiography);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        rvProfilePosts = view.findViewById(R.id.rvProfilePosts);

        tbProfile.inflateMenu(R.menu.menu_profile_toolbar);
        SpannableStringBuilder username = new SpannableStringBuilder(user.getUsername());
        username.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tbProfile.setTitle(username);
        tbProfile.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.miLogout:
                        logout();
                        return true;
                    case R.id.miSettings:
                        return true;
                    default:
                        return ProfileFragment.super.onOptionsItemSelected(item);
                }
            }
        });

        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseFile profilePic = user.getParseFile("Profile");
                Glide.with(getContext()).load(profilePic.getUrl()).circleCrop().into(ivProfilePicture);
            }
        });
        tvProfileBiography.setText(user.getString("Biography"));

        if (user.equals(ParseUser.getCurrentUser())) {
            btnEditProfile.setVisibility(View.VISIBLE);
        } else {
            btnEditProfile.setVisibility(View.GONE);
        }

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), EditProfileActivity.class);
                startActivity(i);
            }
        });

        profilePosts = new ArrayList<>();
        profileAdapter = new ProfileAdapter(getContext(), profilePosts);
        rvProfilePosts.setAdapter(profileAdapter);
        rvProfilePosts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts();
    }

    private void logout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), R.string.logout_error_toast, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Issue with logout", e);
                } else {
                    // TODO: figure out how to log user out from Spotify
                    // delete spotify token from shared preferences
                    editor = (getContext().getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE)).edit();
                    editor.remove("token");
                    editor.commit();

                    goLoginActivity();
                    Toast.makeText(getContext(), R.string.logout_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        ParseUser currentUser = ParseUser.getCurrentUser();
    }

    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder(Post.KEY_CREATED);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // Save received posts to list and notify adapter of new data
                profilePosts.addAll(posts);
                profileAdapter.notifyDataSetChanged();
            }
        });
    }

    private void goLoginActivity() {
        Intent i = new Intent((MainActivity) getContext(), LoginActivity.class);
        startActivity(i);
        ((MainActivity) getContext()).finish();
    }
}