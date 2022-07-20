package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.example.photofy.ColorToGenre;
import com.example.photofy.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    private Toolbar toolbarTop;
    private TextInputEditText etSignupEmail;
    private TextInputEditText etSignupUsername;
    private TextInputEditText etSignupPassword;
    private MultiSpinnerSearch spinnerGenre;
    private Button btnSignup;
    private TextView tvLogin;

    private List<String> genres = new ArrayList<>(ColorToGenre.MOOD_TO_GENRE.values());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Connect visual components with logic
        toolbarTop = findViewById(R.id.toolbarTop);
        etSignupEmail = findViewById(R.id.etSignUpEmail);
        etSignupUsername = findViewById(R.id.etSignUpUsername);
        etSignupPassword = findViewById(R.id.etSignUpPassword);
        btnSignup = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        spinnerGenre = findViewById(R.id.spinnerGenre);

        spinnerGenre.setSearchEnabled(true);
        spinnerGenre.setHintText("Select your three favorite genres");
        spinnerGenre.setSearchHint("Search for genres");
        spinnerGenre.setEmptyTitle("Genre not found!");
        spinnerGenre.setClearText("Clear all");
        Collections.sort(genres);
        spinnerGenre.setItems(populateGenres(genres), new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                    }
                }
            }
        });
        spinnerGenre.setLimit(3, new MultiSpinnerSearch.LimitExceedListener() {
            @Override
            public void onLimitListener(KeyPairBoolData data) {
                Toast.makeText(getApplicationContext(),
                        "Genre limit of 3 exceed ", Toast.LENGTH_LONG).show();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etSignupEmail.getText().toString();
                String username = etSignupUsername.getText().toString();
                String password = etSignupPassword.getText().toString();
                List<String> genreList = getSelectedGenres();
                signupUser(username, password, email, genreList);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

        // on back button pressed
        toolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // Signs up user to Parse
    private void signupUser(String username, String password, String email, List<String> genreList) {
        Log.i(TAG, "Attempting to sign up user " + username);

        ParseUser user = new ParseUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.put("FavGenres", genreList);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with sign up", e);
                    return;
                }
                goSpotifyLoginActivity();
            }
        });
    }

    // populates genres into the dropdown
    private List<KeyPairBoolData> populateGenres(List<String> list) {
        List<KeyPairBoolData> allGenres = new ArrayList<>();

        // set up list of displayed genres as unselected
        for (int i = 0; i < list.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(i + 1);
            keyPairBoolData.setName(list.get(i));
            keyPairBoolData.setSelected(false);
            allGenres.add(keyPairBoolData);
        }

        return allGenres;
    }

    // get selected genres from spinner
    private List<String> getSelectedGenres() {
        List<KeyPairBoolData> selectedGenres = spinnerGenre.getSelectedItems();
        List<String> genreList = new ArrayList<>();
        for (int i = 0; i < selectedGenres.size(); i++) {
            genreList.add(selectedGenres.get(i).getName());
        }
        return genreList;
    }

    private void goSpotifyLoginActivity() {
        Intent i = new Intent (this, SpotifyLoginActivity.class);
        startActivity(i);
        finish();
    }
}