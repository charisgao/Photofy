package com.example.photofy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photofy.PostsAdapter;
import com.example.photofy.R;
import com.example.photofy.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    protected PostsAdapter adapter;
    protected List<Post> allPosts;

    private ViewPager2 viewpagerPosts;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewpagerPosts = view.findViewById(R.id.viewpagerPosts);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);

        // Set the adapter on the ViewPager2
        viewpagerPosts.setAdapter(adapter);

        queryPosts();
    }

    protected void queryPosts() {
        // Specify what type of data we want to query – Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Include data referred by user key
        query.include(Post.KEY_USER);
        // Limit query to 20 items
        query.setLimit(20);
        // Order posts by creation date (newest first)
        query.addDescendingOrder(Post.KEY_CREATED);
        // Start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // Save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

}