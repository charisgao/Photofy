package com.example.photofy.fragments;

import static com.example.photofy.PhotofyApplication.googleCredentials;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.photofy.*;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.activities.SongRecommendationsActivity;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    private Button btnEnableCamera;
    private Button btnUploadImage;
    private ImageView ivCapturedImage;
    private FragmentManager manager;
    private ActivityResultLauncher<String> galleryLauncher;

    private Photo photo;
    private String path;

    private Button btnGetColors;
    private ColorToGenre genreFinder = new ColorToGenre();

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnEnableCamera = view.findViewById(R.id.btnEnableCamera);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        ivCapturedImage = view.findViewById(R.id.ivCapturedImage);

        btnGetColors = view.findViewById(R.id.btnGetColors);
        btnGetColors.setVisibility(View.GONE);

        btnEnableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allPermissionsGranted()) {
                    enableCamera();
                } else {
                    requestPermission();
                }
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGallery();
            }
        });

        manager = ((MainActivity) getContext()).getSupportFragmentManager();
        manager.setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                Log.i(TAG, "inFragmentResult");
                photo = bundle.getParcelable("photo");
                path = bundle.getString("filePath");
                Bitmap bitmap = BitmapFactory.decodeFile(path);

                // Rotate image to be portrait
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                ivCapturedImage.setImageBitmap(rotatedBitmap);
                btnEnableCamera.setVisibility(View.GONE);
                btnUploadImage.setVisibility(View.GONE);
                btnGetColors.setVisibility(View.VISIBLE);
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
                                fos.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
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
                            }
                        });

                        ivCapturedImage.setImageBitmap(resizedBitmap);
                        btnEnableCamera.setVisibility(View.GONE);
                        btnUploadImage.setVisibility(View.GONE);
                        btnGetColors.setVisibility(View.VISIBLE);

                        photo = picture;
                        path = resizedFile.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found error through gallery " + e);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException with gallery " + e);
                    }
                }
            }
        });

        btnGetColors.setOnClickListener(new View.OnClickListener() {
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
                DetectProperties properties = new DetectProperties(googleCredentials, getContext());
                String color = properties.findDominantColor(photo, path);

                photo.setColor(color);
                Log.i(TAG, "generated color " + color);
                photo.saveInBackground();

                String genre = genreFinder.findGenreFromColor(color);
                Log.i(TAG, genre);

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");

                RequestQueue queue = Volley.newRequestQueue(getContext());
                recommendationsService = new RecommendationsService(token, queue);

                recommendationsService.getRecommendations(genre, new RecommendationsCallback() {
                    @Override
                    public void callback(ArrayList<Song> songs) {
                        goToRecommendationsActivity(songs);
                    }
                }, new RecommendationsErrorCallback() {
                    @Override
                    public void callback(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        Thread runner = new Thread(r);
        runner.start();
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
        CameraFragment cameraFragment = new CameraFragment();
        manager.beginTransaction().replace(R.id.flContainer, cameraFragment).addToBackStack(null).commit();
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

    private void goToLoadingFragment() {
        LoadingFragment loadingFragment = new LoadingFragment();
        ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContainer, loadingFragment)
                .addToBackStack(null)
                .commit();
    }

    private void goToRecommendationsActivity(ArrayList<Song> songs) {
        Intent i = new Intent(getContext(), SongRecommendationsActivity.class);
        i.putExtra("photo", photo);
        i.putParcelableArrayListExtra("songs", songs);
        startActivity(i);
    }
}