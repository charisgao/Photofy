package com.example.photofy.fragments;

import static com.example.photofy.PhotofyApplication.googleCredentials;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.photofy.ClosestColor;
import com.example.photofy.DetectProperties;
import com.example.photofy.RecommendationsCallback;
import com.example.photofy.RecommendationsService;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.R;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    private Button btnEnableCamera;
    private ImageView ivCapturedImage;
    private FragmentManager manager;

    private Button btnGetColors;

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
        ivCapturedImage = view.findViewById(R.id.ivCapturedImage);

        btnGetColors = view.findViewById(R.id.btnGetColors);
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

        manager = ((MainActivity) getContext()).getSupportFragmentManager();
        manager.setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                Log.i(TAG, "inFragmentResult");
                String path = bundle.getString("filePath");
                Bitmap bitmap = BitmapFactory.decodeFile(path);

                // Rotate image to be portrait
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                ivCapturedImage.setImageBitmap(rotatedBitmap);
                btnEnableCamera.setVisibility(View.GONE);

                btnGetColors.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Photo picture = bundle.getParcelable("image");

                        goToLoadingFragment();

                        Runnable r = new Runnable() {
                            RecommendationsService recommendationsService;

                            @Override
                            public void run() {
                                DetectProperties getColor = new DetectProperties(picture, path);
                                try  {
                                    String gcsPath = getColor.authExplicit(googleCredentials, getContext());
                                    getColor.detectPropertiesGcs(gcsPath);
                                } catch (Exception e) {
                                    Log.e(TAG, "Credentials error " + e);
                                }
                                ClosestColor alg = new ClosestColor(picture, getContext());
                                Color closestColor = alg.getClosestColor(alg.getDominantColor());
                                String mood = alg.getMood(closestColor);
                                String genre = alg.getGenre(mood);
                                Log.i(TAG, genre);
                                recommendationsService = new RecommendationsService(getContext(), genre);
                                recommendationsService.getRecommendations(new RecommendationsCallback() {
                                    @Override
                                    public void callback() {
                                        ArrayList<Song> songs = recommendationsService.getSongs();
                                        goToRecommendationsFragment(songs);
                                    }
                                });
                            }
                        };
                        Thread runner = new Thread(r);
                        runner.start();
                    }
                });
            }
        });
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

    private void goToLoadingFragment() {
        LoadingFragment loadingFragment = new LoadingFragment();
        ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContainer, loadingFragment)
                .addToBackStack(null)
                .commit();
    }

    private void goToRecommendationsFragment(ArrayList<Song> songs) {
        SongRecommendationsFragment songRecommendationsFragment = new SongRecommendationsFragment();
        Bundle songBundle = new Bundle();
        songBundle.putParcelableArrayList("songs", songs);
        songRecommendationsFragment.setArguments(songBundle);
        ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContainer, songRecommendationsFragment)
                .addToBackStack(null)
                .commit();
    }
}