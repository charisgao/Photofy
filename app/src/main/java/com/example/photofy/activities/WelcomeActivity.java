package com.example.photofy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.photofy.R;
import com.example.photofy.adapters.WelcomeAdapter;
import com.example.photofy.models.Welcome;
import com.parse.ParseClassName;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView indicator1;
    private ImageView indicator2;
    private Button btnWelcomeLogin;
    private Button btnWelcomeContinue;
    private Button btnWelcomeGetStarted;
    private ViewPager2 vpWelcome;

    private WelcomeAdapter adapter;
    private List<Welcome> screens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        vpWelcome = findViewById(R.id.vpWelcome);
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        btnWelcomeLogin = findViewById(R.id.btnWelcomeLogin);
        btnWelcomeContinue = findViewById(R.id.btnWelcomeContinue);
        btnWelcomeGetStarted = findViewById(R.id.btnWelcomeGetStarted);
        btnWelcomeGetStarted.setVisibility(View.GONE);
        vpWelcome = findViewById(R.id.vpWelcome);

        Welcome screen1 = new Welcome(R.drawable.screen1, "Take photos", "Wherever you are, snap images of your surroundings");
        Welcome screen2 = new Welcome(R.drawable.screen2, "Discover new music", "Using our algorithm, we generate you Spotify songs that match the mood associated with your photo");
        screens = new ArrayList<>();
        screens.add(screen1);
        screens.add(screen2);
        adapter = new WelcomeAdapter(this, screens);
        vpWelcome.setAdapter(adapter);

        vpWelcome.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                changeIndicatorColor();
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                changeIndicatorColor();
            }
        });

        btnWelcomeContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vpWelcome.setCurrentItem(1);
            }
        });

        btnWelcomeGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btnWelcomeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void changeIndicatorColor() {
        if (vpWelcome.getCurrentItem() == 0) {
            indicator1.setImageResource(R.color.dark_blue);
            indicator2.setImageResource(R.color.light_gray);
            btnWelcomeContinue.setVisibility(View.VISIBLE);
            btnWelcomeGetStarted.setVisibility(View.GONE);
        } else if (vpWelcome.getCurrentItem() == 1) {
            indicator1.setImageResource(R.color.light_gray);
            indicator2.setImageResource(R.color.dark_blue);
            btnWelcomeContinue.setVisibility(View.GONE);
            btnWelcomeGetStarted.setVisibility(View.VISIBLE);
        }
    }
}