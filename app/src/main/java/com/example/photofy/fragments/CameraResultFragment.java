package com.example.photofy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.example.photofy.models.Image;
import com.parse.ParseFile;

public class CameraResultFragment extends Fragment {

    public static final String TAG = "CameraResultFragment";

    private ImageView ivCapturedImage;

    private Image image;

    public CameraResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivCapturedImage = view.findViewById(R.id.ivCapturedImage);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            image = bundle.getParcelable("image");

            ParseFile picture = image.getImage();
            if (picture != null) {
                // TODO: fix load image (problem with save in background, takes too long, need to find alternative– start fragment for result?)
//                Log.i(TAG, picture.getUrl());
                Glide.with(this.getContext()).load(picture.getUrl()).into(ivCapturedImage);
            }
        }
    }
}