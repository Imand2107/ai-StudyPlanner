package com.studyflow.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.studyflow.app.R;
import com.studyflow.app.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView appName = findViewById(R.id.tvAppName);
        TextView tagline = findViewById(R.id.tvTagline);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        appName.startAnimation(fadeIn);

        AlphaAnimation fadeIn2 = new AlphaAnimation(0.0f, 1.0f);
        fadeIn2.setDuration(1000);
        fadeIn2.setStartOffset(500);
        fadeIn2.setFillAfter(true);
        tagline.startAnimation(fadeIn2);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PreferenceManager prefManager = new PreferenceManager(SplashActivity.this);
            Intent intent;
            if (prefManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
