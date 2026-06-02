package com.studyflow.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studyflow.app.R;
import com.studyflow.app.activities.FocusTimerActivity;
import com.studyflow.app.models.Subject;
import com.studyflow.app.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends Fragment {

    private Button btnStartTimer;
    private LinearLayout llRecentSessions;
    private TextView tvTotalFocusTime, tvSessionsToday;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirestore();
        setupClickListeners();
        loadStats();
    }

    private void initViews(View view) {
        btnStartTimer = view.findViewById(R.id.btnStartTimer);
        llRecentSessions = view.findViewById(R.id.llRecentSessions);
        tvTotalFocusTime = view.findViewById(R.id.tvTotalFocusTime);
        tvSessionsToday = view.findViewById(R.id.tvSessionsToday);
    }

    private void setupFirestore() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        prefManager = new PreferenceManager(requireContext());
    }

    private void setupClickListeners() {
        btnStartTimer.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FocusTimerActivity.class)));
    }

    private void loadStats() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        firestore.collection("study_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalMinutes = 0;
                    int todaySessions = 0;
                    long today = System.currentTimeMillis();
                    long todayStart = today - (today % 86400000L);

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Long duration = doc.getLong("durationMinutes");
                        Long studyDate = doc.getLong("studyDate");
                        if (duration != null) {
                            totalMinutes += duration.intValue();
                            if (studyDate != null && studyDate >= todayStart) {
                                todaySessions++;
                            }
                        }
                    }

                    int hours = totalMinutes / 60;
                    int mins = totalMinutes % 60;
                    tvTotalFocusTime.setText(String.format("%dh %dm", hours, mins));
                    tvSessionsToday.setText(String.valueOf(todaySessions));
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }
}
