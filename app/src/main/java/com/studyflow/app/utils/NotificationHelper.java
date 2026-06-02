package com.studyflow.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.studyflow.app.R;
import com.studyflow.app.activities.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_REMINDERS = "study_reminders";
    private static final String CHANNEL_BREAKS = "break_reminders";
    private static final int NOTIFICATION_REMINDER = 1001;
    private static final int NOTIFICATION_BREAK = 1002;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    context.getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription("Reminders for upcoming study sessions and deadlines");

            NotificationChannel breaksChannel = new NotificationChannel(
                    CHANNEL_BREAKS,
                    context.getString(R.string.notification_channel_breaks),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            breaksChannel.setDescription("Reminders to take breaks during study sessions");

            notificationManager.createNotificationChannel(remindersChannel);
            notificationManager.createNotificationChannel(breaksChannel);
        }
    }

    public void showReminderNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_REMINDER, builder.build());
    }

    public void showBreakNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_BREAKS)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_BREAK, builder.build());
    }

    public void cancelAll() {
        notificationManager.cancelAll();
    }
}
