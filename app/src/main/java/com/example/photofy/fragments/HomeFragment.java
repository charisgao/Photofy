package com.example.photofy.fragments;

import static com.example.photofy.PhotofyApplication.spotifyKey;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.example.photofy.BitmapScaler;
import com.example.photofy.activities.CameraActivity;
import com.example.photofy.activities.ImageResultsActivity;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.adapters.PostsAdapter;
import com.example.photofy.R;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    private static final String CLIENT_ID = spotifyKey;
    private static final String REDIRECT_URI = "intent://";
    public static SpotifyAppRemote mSpotifyAppRemote;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    private ViewPager2 viewpagerPosts;
    private SwipeRefreshLayout swipeContainer;
    private FloatingActionButton fabCompose;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;

    private ActivityResultLauncher<String> galleryLauncher;

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
        fabCompose = view.findViewById(R.id.fabCompose);

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

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
                // TODO: load compose activity with image when done with get colors button
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri selectedImage) {
                if (selectedImage != null){
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), selectedImage);
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                        // Scale the image smaller
                        Bitmap resizedBitmap = BitmapScaler.scaleToFitHeight(bitmap, 200);

                        // Store smaller bitmap to disk
                        // Configure byte output stream
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        // Compress the image further
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                        // Create a new file for the resized bitmap
                        File resizedFile = getPhotoFile();
                        try {
                            resizedFile.createNewFile();
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(resizedFile);
                                // Write the bytes of the bitmap to file
                                fos.write(bytes.toByteArray());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } finally {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Photo picture = new Photo();
                        picture.setUser(ParseUser.getCurrentUser());
                        picture.setImage(new ParseFile(resizedFile));
                        picture.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Intent i = new Intent(getContext(), ImageResultsActivity.class);
                                i.putExtra("filePath", resizedFile.getAbsolutePath());
                                i.putExtra("photo", picture);
                                startActivity(i);
                                Log.i(TAG, "Photo saved successfully");
                            }
                        });
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found error through gallery " + e);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException with gallery " + e);
                    }
                }
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.miOpenCamera:
                        if (allPermissionsGranted()) {
                            enableCamera();
                        } else {
                            requestPermission();
                        }
                        return true;
                    case R.id.miUploadImage:
                        launchGallery();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.menu_compose);
        popup.show();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void enableCamera() {
        Intent i = new Intent(getContext(), CameraActivity.class);
        startActivity(i);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions((MainActivity) getContext(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }

    private void launchGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        if (i.resolveActivity(getContext().getPackageManager()) != null) {
            galleryLauncher.launch("image/*");
        }
    }

    private File getPhotoFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

        // Get safe storage directory for photos
        // getExternalFilesDir used to access package specific directories, so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on timestamp
        return new File(mediaStorageDir.getPath() + File.separator + dateFormat.format(new Date()) + ".jpg");
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