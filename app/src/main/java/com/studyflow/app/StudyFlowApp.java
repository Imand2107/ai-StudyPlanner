package com.studyflow.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudyFlowApp extends Application {
    private static StudyFlowApp instance;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        createNotificationChannels();
    }

    public static StudyFlowApp getInstance() {
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel remindersChannel = new NotificationChannel(
                    "study_reminders",
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription("Reminders for study sessions and deadlines");

            NotificationChannel breaksChannel = new NotificationChannel(
                    "break_reminders",
                    "Break Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            breaksChannel.setDescription("Reminders to take breaks");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(remindersChannel);
                manager.createNotificationChannel(breaksChannel);
            }
        }
    }
}
