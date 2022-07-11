package com.example.photofy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.example.photofy.R;
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

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "CameraActivity";
    private PreviewView previewView;
    private FloatingActionButton fabImageCapture;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor executor;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        executor = Executors.newSingleThreadExecutor();

        previewView = findViewById(R.id.previewView);
        fabImageCapture = findViewById(R.id.fabImageCapture);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error while creating camera", e);
            }
        }, ContextCompat.getMainExecutor(this));

        fabImageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = getPhotoFile();
                Bundle result = new Bundle();

                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Photo photo = new Photo();
                                photo.setUser(ParseUser.getCurrentUser());
                                photo.setImage(new ParseFile(file));
                                photo.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Intent i = new Intent(CameraActivity.this, ImageResultsActivity.class);
                                        i.putExtra("filePath", file.getAbsolutePath());
                                        i.putExtra("photo", photo);
                                        startActivity(i);
                                        Log.i(TAG, "Photo saved successfully");
                                    }
                                });
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
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private File getPhotoFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

        // Get safe storage directory for photos
        // getExternalFilesDir used to access package specific directories, so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on timestamp
        return new File(mediaStorageDir.getPath() + File.separator + dateFormat.format(new Date()) + ".jpg");
    }
}