package com.example.photofy.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.photofy.activities.MainActivity;
import com.example.photofy.R;

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    private Button btnEnableCamera;

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
        ((MainActivity) getContext()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContainer, cameraFragment)
                .addToBackStack(null)
                .commit();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions((MainActivity) getContext(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }
}