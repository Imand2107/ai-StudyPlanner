package com.studyflow.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.studyflow.app.utils.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        String type = intent.getStringExtra("type");
        if (type == null) type = "daily_reminder";

        switch (type) {
            case "daily_reminder":
                notificationHelper.showReminderNotification(
                        "Time to Study!",
                        "Don't forget your study goals for today."
                );
                break;
            case "deadline_reminder":
                String taskName = intent.getStringExtra("taskName");
                if (taskName == null) taskName = "your task";
                notificationHelper.showReminderNotification(
                        "Deadline Approaching!",
                        taskName + " is due soon. Start working on it!"
                );
                break;
            case "break_reminder":
                notificationHelper.showBreakNotification(
                        "Take a Break",
                        "You've been studying for a while. Take a short break!"
                );
                break;
        }
    }
}
