package com.studyflow.app.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StudySession implements Serializable {
    private String sessionId;
    private String userId;
    private String subjectId;
    private long studyDate;
    private int durationMinutes;
    private String sessionType; // pomodoro, custom, scheduled
    private int completedPomodoros;

    public StudySession() {}

    public StudySession(String sessionId, String userId, String subjectId, long studyDate, int durationMinutes, String sessionType) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.subjectId = subjectId;
        this.studyDate = studyDate;
        this.durationMinutes = durationMinutes;
        this.sessionType = sessionType;
        this.completedPomodoros = 0;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public long getStudyDate() { return studyDate; }
    public void setStudyDate(long studyDate) { this.studyDate = studyDate; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public int getCompletedPomodoros() { return completedPomodoros; }
    public void setCompletedPomodoros(int completedPomodoros) { this.completedPomodoros = completedPomodoros; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("userId", userId);
        map.put("subjectId", subjectId);
        map.put("studyDate", studyDate);
        map.put("durationMinutes", durationMinutes);
        map.put("sessionType", sessionType);
        map.put("completedPomodoros", completedPomodoros);
        return map;
    }
}
