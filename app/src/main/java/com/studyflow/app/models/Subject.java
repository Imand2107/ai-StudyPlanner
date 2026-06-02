package com.studyflow.app.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Subject implements Serializable {
    private String subjectId;
    private String userId;
    private String subjectName;
    private int difficultyLevel; // 1=Easy, 2=Medium, 3=Hard, 4=Very Hard
    private String targetGrade;
    private String color;
    private long createdAt;

    public Subject() {}

    public Subject(String subjectId, String userId, String subjectName, int difficultyLevel, String targetGrade, String color) {
        this.subjectId = subjectId;
        this.userId = userId;
        this.subjectName = subjectName;
        this.difficultyLevel = difficultyLevel;
        this.targetGrade = targetGrade;
        this.color = color;
        this.createdAt = System.currentTimeMillis();
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getTargetGrade() { return targetGrade; }
    public void setTargetGrade(String targetGrade) { this.targetGrade = targetGrade; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getDifficultyText() {
        switch (difficultyLevel) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            case 4: return "Very Hard";
            default: return "Medium";
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("subjectId", subjectId);
        map.put("userId", userId);
        map.put("subjectName", subjectName);
        map.put("difficultyLevel", difficultyLevel);
        map.put("targetGrade", targetGrade);
        map.put("color", color);
        map.put("createdAt", createdAt);
        return map;
    }
}
