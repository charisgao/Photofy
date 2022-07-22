package com.example.photofy.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.bumptech.glide.Glide;
import com.example.photofy.BitmapScaler;
import com.example.photofy.ColorToGenre;
import com.example.photofy.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    public static final String TAG = "EditProfileActivity";

    private TextInputEditText etEditName;
    private TextInputEditText etEditUsername;
    private TextInputEditText etEditBio;
    private ImageView ivEditProfilePicture;
    private TextView tvChangePhoto;
    private TextView tvCancel;
    private TextView tvDone;
    private MultiSpinnerSearch spinnerChangeGenre;

    private List<String> genres = new ArrayList<>(ColorToGenre.MOOD_TO_GENRE.values());

    private ActivityResultLauncher<String> galleryLauncher;

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
        spinnerChangeGenre = findViewById(R.id.spinnerChangeGenre);

        ParseUser current = ParseUser.getCurrentUser();
        current.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                etEditName.setText(current.getString("Name"));
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
                showBottomSheetDialog();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
                overridePendingTransition(R.anim.stationary, R.anim.slide_down);
            }
        });

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = etEditName.getText().toString();
                current.put("Name", newName);

                String newUsername = etEditUsername.getText().toString();
                current.put("username", newUsername);

                String newBio = etEditBio.getText().toString();
                current.put("Biography", newBio);

                List<String> genreList = getSelectedGenres();
                current.put("FavGenres", genreList);

                current.saveInBackground();

                ParseFile picture = current.getParseFile("Profile");
                String newGenres = TextUtils.join(", ", genreList);

                Intent i = new Intent();
                i.putExtra("Name", newName);
                i.putExtra("Username", newUsername);
                i.putExtra("Bio", newBio);
                i.putExtra("Genres", newGenres);
                i.putExtra("Picture", picture.getUrl());
                setResult(RESULT_OK, i);
                finish();
                overridePendingTransition(R.anim.stationary, R.anim.slide_down);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri selectedImage) {
                if (selectedImage != null){
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), selectedImage);
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

                        Glide.with(EditProfileActivity.this).load(selectedImage).circleCrop().into(ivEditProfilePicture);

                        ParseFile imageFile = new ParseFile(resizedFile);
                        ParseUser.getCurrentUser().put("Profile", imageFile);
                        ParseUser.getCurrentUser().saveInBackground();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found error through gallery " + e);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException with gallery " + e);
                    }
                }
            }
        });

        spinnerChangeGenre.setSearchEnabled(true);
        spinnerChangeGenre.setHintText("Select your three favorite genres");
        spinnerChangeGenre.setSearchHint("Search for genres");
        spinnerChangeGenre.setEmptyTitle("Genre not found!");
        spinnerChangeGenre.setClearText("Clear all");
        Collections.sort(genres);
        spinnerChangeGenre.setItems(populateGenres(genres), new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                    }
                }
            }
        });
        spinnerChangeGenre.setLimit(3, new MultiSpinnerSearch.LimitExceedListener() {
            @Override
            public void onLimitListener(KeyPairBoolData data) {
                Toast.makeText(getApplicationContext(),
                        "Genre limit of 3 exceed ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.change_profile_bottom_sheet_dialog);

        Button btnPfpRemove = bottomSheetDialog.findViewById(R.id.btnPfpRemove);
        Button btnPfpNew = bottomSheetDialog.findViewById(R.id.btnPfpNew);
        Button btnPfpUpload = bottomSheetDialog.findViewById(R.id.btnPfpUpload);

        btnPfpRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.defaultpfp);
                Glide.with(EditProfileActivity.this).load(uri).circleCrop().into(ivEditProfilePicture);

                File file = new File(uri.getPath());
                ParseFile defaultFile = new ParseFile(file);
                ParseUser.getCurrentUser().put("Profile", defaultFile);

                ParseUser.getCurrentUser().saveInBackground();
                bottomSheetDialog.dismiss();
            }
        });

        btnPfpNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: allow user to take new photo for pfp
                bottomSheetDialog.dismiss();
            }
        });

        btnPfpUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                if (i.resolveActivity(getPackageManager()) != null) {
                    galleryLauncher.launch("image/*");
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private File getPhotoFile() {
        // Get safe storage directory for photos
        // getExternalFilesDir used to access package specific directories, so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on timestamp
        return new File(mediaStorageDir.getPath() + File.separator + "profile.jpg");
    }


    // populates genres into the dropdown
    private List<KeyPairBoolData> populateGenres(List<String> list) {
        List<KeyPairBoolData> allGenres = new ArrayList<>();
        List<String> currentFaves = ParseUser.getCurrentUser().getList("FavGenres");

        // set up list of displayed genres
        for (int i = 0; i < list.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(i + 1);
            keyPairBoolData.setName(list.get(i));
            keyPairBoolData.setSelected(currentFaves.contains(list.get(i)));
            allGenres.add(keyPairBoolData);
        }

        return allGenres;
    }

    // get selected genres from spinner
    private List<String> getSelectedGenres() {
        List<KeyPairBoolData> selectedGenres = spinnerChangeGenre.getSelectedItems();
        List<String> genreList = new ArrayList<>();
        for (int i = 0; i < selectedGenres.size(); i++) {
            genreList.add(selectedGenres.get(i).getName());
        }
        return genreList;
    }
}