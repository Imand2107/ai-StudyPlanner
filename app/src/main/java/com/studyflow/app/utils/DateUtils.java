package com.studyflow.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public static String formatDay(long timestamp) {
        return dayFormat.format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public static String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        return dateTimeFormat.format(new Date(timestamp));
    }

    public static String formatShortDate(long timestamp) {
        return shortDateFormat.format(new Date(timestamp));
    }

    public static String getTimeRemaining(long dueDate) {
        long diff = dueDate - System.currentTimeMillis();
        if (diff < 0) return "Overdue";

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        if (days > 0) {
            return days + "d " + hours + "h left";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m left";
        } else {
            return minutes + "m left";
        }
    }

    public static boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isTomorrow(long timestamp) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        return tomorrow.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isThisWeek(long timestamp) {
        long now = System.currentTimeMillis();
        long weekEnd = now + 604800000L;
        return timestamp >= now && timestamp < weekEnd;
    }

    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return hours + " hr";
        }
        return hours + " hr " + mins + " min";
    }

    public static String formatHours(float hours) {
        if (hours < 1) {
            return Math.round(hours * 60) + " min";
        }
        return String.format(Locale.getDefault(), "%.1f hr", hours);
    }
}
