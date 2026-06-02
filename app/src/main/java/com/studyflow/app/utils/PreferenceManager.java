package com.studyflow.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "studyflow_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_STUDY_STREAK = "study_streak";
    private static final String KEY_LAST_STUDY_DATE = "last_study_date";
    private static final String KEY_AVAILABLE_HOURS = "available_hours";
    private static final String KEY_POMODORO_DURATION = "pomodoro_duration";
    private static final String KEY_SHORT_BREAK_DURATION = "short_break_duration";
    private static final String KEY_LONG_BREAK_DURATION = "long_break_duration";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setStudyStreak(int streak) {
        editor.putInt(KEY_STUDY_STREAK, streak);
        editor.apply();
    }

    public int getStudyStreak() {
        return prefs.getInt(KEY_STUDY_STREAK, 0);
    }

    public void setLastStudyDate(long date) {
        editor.putLong(KEY_LAST_STUDY_DATE, date);
        editor.apply();
    }

    public long getLastStudyDate() {
        return prefs.getLong(KEY_LAST_STUDY_DATE, 0);
    }

    public void setAvailableHours(int hours) {
        editor.putInt(KEY_AVAILABLE_HOURS, hours);
        editor.apply();
    }

    public int getAvailableHours() {
        return prefs.getInt(KEY_AVAILABLE_HOURS, 4);
    }

    public void setPomodoroDuration(int minutes) {
        editor.putInt(KEY_POMODORO_DURATION, minutes);
        editor.apply();
    }

    public int getPomodoroDuration() {
        return prefs.getInt(KEY_POMODORO_DURATION, 25);
    }

    public void setShortBreakDuration(int minutes) {
        editor.putInt(KEY_SHORT_BREAK_DURATION, minutes);
        editor.apply();
    }

    public int getShortBreakDuration() {
        return prefs.getInt(KEY_SHORT_BREAK_DURATION, 5);
    }

    public void setLongBreakDuration(int minutes) {
        editor.putInt(KEY_LONG_BREAK_DURATION, minutes);
        editor.apply();
    }

    public int getLongBreakDuration() {
        return prefs.getInt(KEY_LONG_BREAK_DURATION, 15);
    }

    public void updateStudyStreak() {
        long lastDate = getLastStudyDate();
        long now = System.currentTimeMillis();
        long dayMs = 86400000L;

        long todayStart = now - (now % dayMs);
        long lastStart = lastDate - (lastDate % dayMs);

        if (lastStart == todayStart) {
            return;
        } else if (lastStart == todayStart - dayMs) {
            setStudyStreak(getStudyStreak() + 1);
        } else {
            setStudyStreak(1);
        }
        setLastStudyDate(now);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
