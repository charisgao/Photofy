package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.photofy.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseUser;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etSettingsNewPass;
    private TextInputEditText etSettingsConfirmPass;
    private Button btnSettingsCancel;
    private Button btnSettingsUpdate;

    private ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etSettingsNewPass = findViewById(R.id.etSettingsNewPass);
        etSettingsConfirmPass = findViewById(R.id.etSettingsConfirmPass);
        btnSettingsCancel = findViewById(R.id.btnSettingsCancel);
        btnSettingsUpdate = findViewById(R.id.btnSettingsUpdate);

        btnSettingsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
            }
        });

        btnSettingsUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPass = etSettingsNewPass.getText().toString();
                String confirmPass = etSettingsConfirmPass.getText().toString();
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(SettingsActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                } else if (newPass.length() == 0) {
                    Toast.makeText(SettingsActivity.this, "Password field cannot be blank", Toast.LENGTH_SHORT).show();
                }
                else {
                    updatePassword(newPass);
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });
    }

    private void updatePassword(String password) {
        currentUser.setPassword(password);
        currentUser.saveInBackground();
    }
}