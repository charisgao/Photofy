package com.example.photofy.fragments;

import static com.example.photofy.PhotofyApplication.spotifyKey;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photofy.activities.MainActivity;
import com.example.photofy.adapters.PostsAdapter;
import com.example.photofy.R;
import com.example.photofy.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    private static final String CLIENT_ID = spotifyKey;
    private static final String REDIRECT_URI = "intent://";
    public static SpotifyAppRemote mSpotifyAppRemote;

    private ViewPager2 viewpagerPosts;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private SwipeRefreshLayout swipeContainer;

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
        swipeContainer = view.findViewById(R.id.swipeContainer);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts, Navigation.findNavController(requireActivity(), R.id.navHostFragment));

        // Set the adapter on the ViewPager2
        viewpagerPosts.setAdapter(adapter);

        connectSpotifyAppRemote();
        queryPosts();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryPosts();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void connectSpotifyAppRemote() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        Connector.ConnectionListener connectionListener = new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "SpotifyAppRemote connection error");
            }
        };

        SpotifyAppRemote.connect(getContext(), connectionParams, connectionListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        connectSpotifyAppRemote();
    }

    @Override
    public void onPause() {
        super.onPause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
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