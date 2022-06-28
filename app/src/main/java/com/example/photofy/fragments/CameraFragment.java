package com.example.photofy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photofy.R;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.models.Photo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// TODO: need to make into an activity (do not need bottom navigation bar showing)
public class CameraFragment extends Fragment {

    public static final String TAG = "CameraFragment";
    private PreviewView previewView;
    private FloatingActionButton fabImageCapture;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor executor;
    private ImageCapture imageCapture;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        previewView = view.findViewById(R.id.previewView);
        fabImageCapture = view.findViewById(R.id.fabImageCapture);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error while creating camera", e);
            }
        }, ContextCompat.getMainExecutor(getContext()));

        fabImageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = getPhotoFile();
                Bundle result = new Bundle();

                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            Photo picture = new Photo();
                            picture.setUser(ParseUser.getCurrentUser());
                            picture.setImage(new ParseFile(file));
                            picture.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                }
                            });
                            result.putString("filePath", file.getAbsolutePath());
                            result.putParcelable("image", picture);
                            FragmentManager manager = ((MainActivity) getContext()).getSupportFragmentManager();
                            manager.setFragmentResult("requestKey", result);
                            ComposeFragment composeFragment = new ComposeFragment();
                            manager.beginTransaction()
                                    .replace(R.id.flContainer, composeFragment)
                                    .addToBackStack(null)
                                    .commit();

                            Log.i(TAG, "Photo saved successfully");
                        }
                        @Override
                        public void onError(ImageCaptureException error) {
                            Log.e(TAG, "Error while capturing camera image", error);
                        }
                    }
                );
            }
        });
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(getActivity().getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
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
}