package com.studyflow.app.utils;

import android.content.Context;

import com.studyflow.app.R;

public class ColorUtils {
    public static int[] PRIORITY_COLORS = {
            0, // unused
            0xFFFF4757, // High
            0xFFFFA502, // Medium
            0xFF2ED573  // Low
    };

    public static int[] DIFFICULTY_COLORS = {
            0,
            0xFF2ED573, // Easy
            0xFFFFA502, // Medium
            0xFFFF6B6B, // Hard
            0xFFFF4757  // Very Hard
    };

    public static int[] SUBJECT_COLORS = {
            0xFF6C63FF, // Purple
            0xFFFF6584, // Pink
            0xFFFFA502, // Orange
            0xFF2ED573, // Green
            0xFF3B82F6, // Blue
            0xFF00BCD4, // Cyan
            0xFF9C27B0, // Deep Purple
            0xFFE91E63, // Rose
            0xFF009688, // Teal
            0xFFFF5722  // Deep Orange
    };

    public static int[] TYPE_COLORS = {
            0xFF6C63FF, // Assignment
            0xFFFF6584, // Quiz
            0xFFFFA502, // Project
            0xFFFF4757  // Exam
    };

    public static int getPriorityColor(int priority) {
        if (priority >= 0 && priority < PRIORITY_COLORS.length) {
            return PRIORITY_COLORS[priority];
        }
        return PRIORITY_COLORS[2];
    }

    public static int getDifficultyColor(int difficulty) {
        if (difficulty >= 0 && difficulty < DIFFICULTY_COLORS.length) {
            return DIFFICULTY_COLORS[difficulty];
        }
        return DIFFICULTY_COLORS[2];
    }

    public static int getSubjectColor(int index) {
        return SUBJECT_COLORS[index % SUBJECT_COLORS.length];
    }

    public static int getSubjectColor(String colorHex) {
        try {
            return (int) Long.parseLong(colorHex.replace("#", ""), 16) | 0xFF000000;
        } catch (Exception e) {
            return SUBJECT_COLORS[0];
        }
    }

    public static int getTypeColor(String type) {
        if (type == null) return TYPE_COLORS[0];
        switch (type.toLowerCase()) {
            case "assignment": return TYPE_COLORS[0];
            case "quiz": return TYPE_COLORS[1];
            case "project": return TYPE_COLORS[2];
            case "exam": return TYPE_COLORS[3];
            default: return TYPE_COLORS[0];
        }
    }

    public static String colorToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
