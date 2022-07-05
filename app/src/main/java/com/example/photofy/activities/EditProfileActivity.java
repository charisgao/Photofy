package com.example.photofy.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class EditProfileActivity extends AppCompatActivity {

    public static final String TAG = "EditProfileActivity";

    private TextInputEditText etEditName;
    private TextInputEditText etEditUsername;
    private TextInputEditText etEditBio;
    private ImageView ivEditProfilePicture;
    private TextView tvCancel;
    private TextView tvDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Connect visual components with logic
        etEditName = findViewById(R.id.etEditName);
        etEditUsername = findViewById(R.id.etEditUsername);
        etEditBio = findViewById(R.id.etEditBio);
        ivEditProfilePicture = findViewById(R.id.ivEditProfilePicture);
        tvCancel = findViewById(R.id.tvCancel);
        tvDone = findViewById(R.id.tvDone);

        ParseUser current = ParseUser.getCurrentUser();
        current.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                etEditUsername.setText(current.getUsername());
                etEditBio.setText(current.getString("Biograpy"));
            }
        });

        ParseFile profile = current.getParseFile("Profile");
        if (profile != null) {
            Glide.with(this).load(profile.getUrl()).circleCrop().into(ivEditProfilePicture);
        }

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = etEditUsername.getText().toString();
                current.put("username", newUsername);

                String newBio = etEditBio.getText().toString();
                current.put("Biography", newBio);

                current.saveInBackground();
            }
        });
    }
}