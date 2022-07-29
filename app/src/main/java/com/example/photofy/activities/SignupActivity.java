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
    private TextInputEditText etSignupFirstName;
    private TextInputEditText etSignUpLastName;
    private TextInputEditText etSignupEmail;
    private TextInputEditText etSignupUsername;
    private TextInputEditText etSignupPassword;
    private Button btnSignup;
    private TextView tvLogin;

    private final List<String> genres = new ArrayList<>(ColorToGenre.MOOD_TO_GENRE.values());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        toolbarTop = findViewById(R.id.toolbarTop);
        etSignupFirstName = findViewById(R.id.etSignUpFirstName);
        etSignUpLastName = findViewById(R.id.etSignUpLastName);
        etSignupEmail = findViewById(R.id.etSignUpEmail);
        etSignupUsername = findViewById(R.id.etSignUpUsername);
        etSignupPassword = findViewById(R.id.etSignUpPassword);
        btnSignup = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etSignupFirstName.getText().toString() + " " + etSignUpLastName.getText().toString();
                String email = etSignupEmail.getText().toString();
                String username = etSignupUsername.getText().toString();
                String password = etSignupPassword.getText().toString();
                signupUser(name, username, password, email);
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
    private void signupUser(String name, String username, String password, String email) {
        Log.i(TAG, "Attempting to sign up user " + username);

        ParseUser user = new ParseUser();
        user.put("Name", name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with sign up", e);
                    return;
                }
                goSelectFavGenresActivity();
            }
        });
    }

    private void goSelectFavGenresActivity() {
        Intent i = new Intent (this, SelectFavGenresActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}