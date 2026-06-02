package com.studyflow.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StudySchedule implements Serializable {
    private String scheduleId;
    private String userId;
    private long generatedAt;
    private int availableHoursPerDay;
    private List<ScheduleDay> weeklySchedule;
    private List<SubjectPriority> subjectPriorities;
    private List<RecommendedSession> recommendedSessions;

    public StudySchedule() {
        weeklySchedule = new ArrayList<>();
        subjectPriorities = new ArrayList<>();
        recommendedSessions = new ArrayList<>();
    }

    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }

    public int getAvailableHoursPerDay() { return availableHoursPerDay; }
    public void setAvailableHoursPerDay(int availableHoursPerDay) { this.availableHoursPerDay = availableHoursPerDay; }

    public List<ScheduleDay> getWeeklySchedule() { return weeklySchedule; }
    public void setWeeklySchedule(List<ScheduleDay> weeklySchedule) { this.weeklySchedule = weeklySchedule; }

    public List<SubjectPriority> getSubjectPriorities() { return subjectPriorities; }
    public void setSubjectPriorities(List<SubjectPriority> subjectPriorities) { this.subjectPriorities = subjectPriorities; }

    public List<RecommendedSession> getRecommendedSessions() { return recommendedSessions; }
    public void setRecommendedSessions(List<RecommendedSession> recommendedSessions) { this.recommendedSessions = recommendedSessions; }

    public static class ScheduleDay implements Serializable {
        private String dayName;
        private List<ScheduleSlot> slots;

        public ScheduleDay() {
            slots = new ArrayList<>();
        }

        public ScheduleDay(String dayName) {
            this.dayName = dayName;
            this.slots = new ArrayList<>();
        }

        public String getDayName() { return dayName; }
        public void setDayName(String dayName) { this.dayName = dayName; }

        public List<ScheduleSlot> getSlots() { return slots; }
        public void setSlots(List<ScheduleSlot> slots) { this.slots = slots; }
    }

    public static class ScheduleSlot implements Serializable {
        private String subjectId;
        private String subjectName;
        private String startTime;
        private String endTime;
        private int durationMinutes;
        private String color;

        public ScheduleSlot() {}

        public ScheduleSlot(String subjectId, String subjectName, String startTime, String endTime, int durationMinutes, String color) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
            this.color = color;
        }

        public String getSubjectId() { return subjectId; }
        public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class SubjectPriority implements Serializable {
        private String subjectId;
        private String subjectName;
        private int priorityRank;
        private float recommendedHours;
        private String reason;
        private String color;

        public SubjectPriority() {}

        public SubjectPriority(String subjectId, String subjectName, int priorityRank, float recommendedHours, String reason, String color) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.priorityRank = priorityRank;
            this.recommendedHours = recommendedHours;
            this.reason = reason;
            this.color = color;
        }

        public String getSubjectId() { return subjectId; }
        public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public int getPriorityRank() { return priorityRank; }
        public void setPriorityRank(int priorityRank) { this.priorityRank = priorityRank; }

        public float getRecommendedHours() { return recommendedHours; }
        public void setRecommendedHours(float recommendedHours) { this.recommendedHours = recommendedHours; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class RecommendedSession implements Serializable {
        private String subjectId;
        private String subjectName;
        private String suggestion;
        private int recommendedMinutes;
        private String bestTime;
        private String color;

        public RecommendedSession() {}

        public RecommendedSession(String subjectId, String subjectName, String suggestion, int recommendedMinutes, String bestTime, String color) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.suggestion = suggestion;
            this.recommendedMinutes = recommendedMinutes;
            this.bestTime = bestTime;
            this.color = color;
        }

        public String getSubjectId() { return subjectId; }
        public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

        public int getRecommendedMinutes() { return recommendedMinutes; }
        public void setRecommendedMinutes(int recommendedMinutes) { this.recommendedMinutes = recommendedMinutes; }

        public String getBestTime() { return bestTime; }
        public void setBestTime(String bestTime) { this.bestTime = bestTime; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}
