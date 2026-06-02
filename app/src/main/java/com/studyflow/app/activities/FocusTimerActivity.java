package com.studyflow.app.activities;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studyflow.app.R;
import com.studyflow.app.models.StudySession;
import com.studyflow.app.utils.AlarmHelper;
import com.studyflow.app.utils.DateUtils;
import com.studyflow.app.utils.NotificationHelper;
import com.studyflow.app.utils.PreferenceManager;

import java.util.UUID;

public class FocusTimerActivity extends AppCompatActivity {

    private TextView tvTimer, tvStatus, tvSessionsCompleted, tvSubjectLabel;
    private ProgressBar progressBar;
    private Button btnStartPause, btnReset, btnSkip;
    private LinearLayout llSessionInfo;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private boolean isBreak = false;
    private long timeLeftInMillis;
    private int sessionsCompleted = 0;

    private int pomodoroDuration;
    private int shortBreakDuration;
    private int longBreakDuration;

    private PreferenceManager prefManager;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private AlarmHelper alarmHelper;
    private NotificationHelper notificationHelper;
    private long sessionStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_timer);

        prefManager = new PreferenceManager(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        alarmHelper = new AlarmHelper(this);
        notificationHelper = new NotificationHelper(this);

        pomodoroDuration = prefManager.getPomodoroDuration();
        shortBreakDuration = prefManager.getShortBreakDuration();
        longBreakDuration = prefManager.getLongBreakDuration();

        initViews();
        setupClickListeners();
        resetTimer();
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        tvSessionsCompleted = findViewById(R.id.tvSessionsCompleted);
        tvSubjectLabel = findViewById(R.id.tvSubjectLabel);
        progressBar = findViewById(R.id.progressBar);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnSkip = findViewById(R.id.btnSkip);
        llSessionInfo = findViewById(R.id.llSessionInfo);
    }

    private void setupClickListeners() {
        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        btnSkip.setOnClickListener(v -> skipToNext());

        findViewById(R.id.ivBack).setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            finish();
        });
    }

    private void startTimer() {
        sessionStartTime = System.currentTimeMillis();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                btnStartPause.setText(R.string.start);
                playNotificationSound();

                if (!isBreak) {
                    sessionsCompleted++;
                    tvSessionsCompleted.setText(String.valueOf(sessionsCompleted));
                    saveStudySession();
                    prefManager.updateStudyStreak();

                    if (sessionsCompleted % 4 == 0) {
                        startBreak(longBreakDuration * 60000L);
                        tvStatus.setText(R.string.long_break);
                    } else {
                        startBreak(shortBreakDuration * 60000L);
                        tvStatus.setText(R.string.short_break);
                    }
                } else {
                    isBreak = false;
                    tvStatus.setText(R.string.pomodoro);
                    timeLeftInMillis = pomodoroDuration * 60000L;
                    updateTimerDisplay();
                    notificationHelper.showBreakNotification(
                            "Break Over!",
                            "Time to focus again. You've completed " + sessionsCompleted + " sessions!"
                    );
                }
            }
        }.start();

        isRunning = true;
        btnStartPause.setText(R.string.pause);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        btnStartPause.setText(R.string.start);
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        isBreak = false;
        timeLeftInMillis = pomodoroDuration * 60000L;
        tvStatus.setText(R.string.pomodoro);
        updateTimerDisplay();
        btnStartPause.setText(R.string.start);
        tvSessionsCompleted.setText(String.valueOf(sessionsCompleted));
    }

    private void startBreak(long breakDuration) {
        isBreak = true;
        timeLeftInMillis = breakDuration;
        updateTimerDisplay();
        startTimer();
    }

    private void skipToNext() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (!isBreak) {
            sessionsCompleted++;
            tvSessionsCompleted.setText(String.valueOf(sessionsCompleted));
            if (sessionsCompleted % 4 == 0) {
                startBreak(longBreakDuration * 60000L);
                tvStatus.setText(R.string.long_break);
            } else {
                startBreak(shortBreakDuration * 60000L);
                tvStatus.setText(R.string.short_break);
            }
        } else {
            isBreak = false;
            timeLeftInMillis = pomodoroDuration * 60000L;
            tvStatus.setText(R.string.pomodoro);
            updateTimerDisplay();
        }
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));

        int totalMillis = isBreak ?
                (isLongBreak() ? longBreakDuration : shortBreakDuration) * 60000 :
                pomodoroDuration * 60000;
        int progress = (int) ((totalMillis - timeLeftInMillis) * 100 / totalMillis);
        progressBar.setProgress(progress);
    }

    private boolean isLongBreak() {
        return sessionsCompleted > 0 && sessionsCompleted % 4 == 0;
    }

    private void saveStudySession() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        String sessionId = UUID.randomUUID().toString();
        StudySession session = new StudySession(
                sessionId,
                userId,
                "",
                sessionStartTime,
                pomodoroDuration,
                "pomodoro"
        );
        session.setCompletedPomodoros(1);

        firestore.collection("study_sessions").document(sessionId)
                .set(session.toMap());
    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
