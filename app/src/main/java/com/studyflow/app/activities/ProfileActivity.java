package com.studyflow.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studyflow.app.R;
import com.studyflow.app.utils.AlarmHelper;
import com.studyflow.app.utils.PreferenceManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvVersion;
    private Switch switchDarkMode, switchNotifications;
    private LinearLayout llLogout, llAbout;
    private PreferenceManager prefManager;
    private FirebaseAuth auth;
    private AlarmHelper alarmHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefManager = new PreferenceManager(this);
        auth = FirebaseAuth.getInstance();
        alarmHelper = new AlarmHelper(this);

        initViews();
        loadUserData();
        setupSwitches();
        setupClickListeners();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvVersion = findViewById(R.id.tvVersion);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        llLogout = findViewById(R.id.llLogout);
        llAbout = findViewById(R.id.llAbout);
    }

    private void loadUserData() {
        tvName.setText(prefManager.getUserName());
        tvEmail.setText(prefManager.getUserEmail());
        tvVersion.setText(R.string.version);
    }

    private void setupSwitches() {
        switchDarkMode.setChecked(prefManager.isDarkMode());
        switchNotifications.setChecked(prefManager.isNotificationsEnabled());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setDarkMode(isChecked);
            Toast.makeText(this, "Restart app to apply theme", Toast.LENGTH_SHORT).show();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setNotificationsEnabled(isChecked);
            if (isChecked) {
                alarmHelper.setDailyReminder(8, 0, 9999);
                Toast.makeText(this, "Daily reminders enabled", Toast.LENGTH_SHORT).show();
            } else {
                alarmHelper.cancelAlarm(9999);
                Toast.makeText(this, "Daily reminders disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        llLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        auth.signOut();
                        prefManager.clearAll();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        llAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("About StudyFlow")
                    .setMessage("StudyFlow v1.0\n\nAI-powered study planner designed to help students manage their academic workload and improve productivity.\n\nFeatures:\n- AI Study Schedule Generator\n- Focus Timer (Pomodoro)\n- Task & Subject Management\n- Progress Tracking\n- Analytics Dashboard")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }
}
