package com.example.photofy.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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
    private TextView tvChangePhoto;
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
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        tvCancel = findViewById(R.id.tvCancel);
        tvDone = findViewById(R.id.tvDone);

        ParseUser current = ParseUser.getCurrentUser();
        current.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                etEditUsername.setText(current.getUsername());
                etEditBio.setText(current.getString("Biography"));
            }
        });

        ParseFile profile = current.getParseFile("Profile");
        if (profile != null) {
            Glide.with(this).load(profile.getUrl()).circleCrop().into(ivEditProfilePicture);
        }

        tvChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setMessage("Do you want to change your profile picture?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO: launch camera
                                dialog.cancel();
                            }
                        });

                builder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                overridePendingTransition(R.anim.stationary, R.anim.bottom_down);
                finish();
            }
        });

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = etEditUsername.getText().toString();
                current.put("username", newUsername);

                String newBio = etEditBio.getText().toString();
                current.put("Biography", newBio);

                current.saveInBackground();

                Intent i = new Intent();
                i.putExtra("Username", newUsername);
                i.putExtra("Bio", newBio);
                setResult(RESULT_OK, i);
                overridePendingTransition(R.anim.stationary, R.anim.bottom_down);
                finish();
            }
        });
    }
}