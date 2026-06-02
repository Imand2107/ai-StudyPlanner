package com.studyflow.app.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Task implements Serializable {
    private String taskId;
    private String subjectId;
    private String userId;
    private String taskName;
    private String taskType; // assignment, quiz, project, exam
    private long dueDate;
    private int priority; // 1=High, 2=Medium, 3=Low
    private int status; // 0=Pending, 1=In Progress, 2=Completed
    private long createdAt;

    public Task() {}

    public Task(String taskId, String subjectId, String userId, String taskName, String taskType,
                long dueDate, int priority, int status) {
        this.taskId = taskId;
        this.subjectId = subjectId;
        this.userId = userId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getPriorityText() {
        switch (priority) {
            case 1: return "High";
            case 2: return "Medium";
            case 3: return "Low";
            default: return "Medium";
        }
    }

    public String getStatusText() {
        switch (status) {
            case 0: return "Pending";
            case 1: return "In Progress";
            case 2: return "Completed";
            default: return "Pending";
        }
    }

    public boolean isOverdue() {
        return dueDate < System.currentTimeMillis() && status != 2;
    }

    public boolean isDueToday() {
        long todayStart = getDayStart(System.currentTimeMillis());
        long todayEnd = todayStart + 86400000L;
        return dueDate >= todayStart && dueDate < todayEnd;
    }

    public boolean isDueThisWeek() {
        long now = System.currentTimeMillis();
        long weekEnd = now + 604800000L;
        return dueDate >= now && dueDate < weekEnd;
    }

    private long getDayStart(long timestamp) {
        return timestamp - (timestamp % 86400000L);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("subjectId", subjectId);
        map.put("userId", userId);
        map.put("taskName", taskName);
        map.put("taskType", taskType);
        map.put("dueDate", dueDate);
        map.put("priority", priority);
        map.put("status", status);
        map.put("createdAt", createdAt);
        return map;
    }
}
