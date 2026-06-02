package com.studyflow.app.ai;

import com.studyflow.app.models.Subject;
import com.studyflow.app.models.Task;
import com.studyflow.app.models.StudySchedule;
import com.studyflow.app.models.StudySchedule.ScheduleDay;
import com.studyflow.app.models.StudySchedule.ScheduleSlot;
import com.studyflow.app.models.StudySchedule.SubjectPriority;
import com.studyflow.app.models.StudySchedule.RecommendedSession;
import com.studyflow.app.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScheduleGenerator {

    public static StudySchedule generateSchedule(List<Subject> subjects, List<Task> tasks, int availableHoursPerDay) {
        StudySchedule schedule = new StudySchedule();
        schedule.setAvailableHoursPerDay(availableHoursPerDay);
        schedule.setGeneratedAt(System.currentTimeMillis());

        List<SubjectPriority> priorities = calculateSubjectPriorities(subjects, tasks);
        schedule.setSubjectPriorities(priorities);

        List<RecommendedSession> sessions = generateRecommendedSessions(subjects, tasks, availableHoursPerDay);
        schedule.setRecommendedSessions(sessions);

        List<ScheduleDay> weeklySchedule = generateWeeklySchedule(subjects, tasks, priorities, availableHoursPerDay);
        schedule.setWeeklySchedule(weeklySchedule);

        return schedule;
    }

    private static List<SubjectPriority> calculateSubjectPriorities(List<Subject> subjects, List<Task> tasks) {
        List<SubjectPriority> priorities = new ArrayList<>();

        for (Subject subject : subjects) {
            int urgentTaskCount = 0;
            float totalHours = 0;

            for (Task task : tasks) {
                if (task.getSubjectId().equals(subject.getSubjectId()) && task.getStatus() != 2) {
                    if (task.isDueThisWeek()) {
                        urgentTaskCount++;
                    }
                    totalHours += estimateTaskHours(task);
                }
            }

            float score = calculatePriorityScore(subject.getDifficultyLevel(), urgentTaskCount, totalHours);
            String reason = generatePriorityReason(subject.getDifficultyLevel(), urgentTaskCount, totalHours);

            SubjectPriority priority = new SubjectPriority(
                    subject.getSubjectId(),
                    subject.getSubjectName(),
                    0,
                    totalHours,
                    reason,
                    subject.getColor()
            );
            priorities.add(priority);
        }

        Collections.sort(priorities, (a, b) -> Float.compare(b.getRecommendedHours(), a.getRecommendedHours()));

        for (int i = 0; i < priorities.size(); i++) {
            priorities.get(i).setPriorityRank(i + 1);
        }

        return priorities;
    }

    private static float calculatePriorityScore(int difficulty, int urgentTasks, float estimatedHours) {
        float difficultyWeight = difficulty * 2.5f;
        float urgencyWeight = urgentTasks * 3.0f;
        float hoursWeight = estimatedHours * 0.5f;
        return difficultyWeight + urgencyWeight + hoursWeight;
    }

    private static String generatePriorityReason(int difficulty, int urgentTasks, float hours) {
        StringBuilder reason = new StringBuilder();

        if (urgentTasks > 0) {
            reason.append(urgentTasks).append(" urgent task(s) this week");
        }

        if (difficulty >= 3) {
            if (reason.length() > 0) reason.append(" & ");
            reason.append("High difficulty subject");
        }

        if (hours > 10) {
            if (reason.length() > 0) reason.append(" & ");
            reason.append("Heavy workload");
        }

        if (reason.length() == 0) {
            reason.append("Maintain consistent study");
        }

        return reason.toString();
    }

    private static float estimateTaskHours(Task task) {
        switch (task.getTaskType() != null ? task.getTaskType() : "") {
            case "exam": return 8f;
            case "project": return 12f;
            case "assignment": return 4f;
            case "quiz": return 2f;
            default: return 3f;
        }
    }

    private static List<RecommendedSession> generateRecommendedSessions(
            List<Subject> subjects, List<Task> tasks, int availableHours) {

        List<RecommendedSession> sessions = new ArrayList<>();

        for (Subject subject : subjects) {
            int pendingTasks = 0;
            boolean hasUrgentTask = false;

            for (Task task : tasks) {
                if (task.getSubjectId().equals(subject.getSubjectId()) && task.getStatus() != 2) {
                    pendingTasks++;
                    if (task.isDueThisWeek() || task.isOverdue()) {
                        hasUrgentTask = true;
                    }
                }
            }

            if (pendingTasks > 0) {
                int recommendedMinutes = calculateRecommendedMinutes(
                        subject.getDifficultyLevel(), pendingTasks, hasUrgentTask, availableHours);

                String suggestion = generateSessionSuggestion(
                        subject.getSubjectName(), subject.getDifficultyLevel(), pendingTasks, hasUrgentTask);

                String bestTime = hasUrgentTask ? "Morning" : "Afternoon";

                sessions.add(new RecommendedSession(
                        subject.getSubjectId(),
                        subject.getSubjectName(),
                        suggestion,
                        recommendedMinutes,
                        bestTime,
                        subject.getColor()
                ));
            }
        }

        Collections.sort(sessions, (a, b) -> b.getRecommendedMinutes() - a.getRecommendedMinutes());
        return sessions;
    }

    private static int calculateRecommendedMinutes(int difficulty, int pendingTasks, boolean urgent, int availableHours) {
        int baseMinutes = 30;
        int difficultyMultiplier = difficulty * 10;
        int taskMultiplier = pendingTasks * 5;
        int urgentBonus = urgent ? 15 : 0;

        int total = baseMinutes + difficultyMultiplier + taskMultiplier + urgentBonus;
        int maxMinutes = availableHours * 60 / Math.max(1, pendingTasks);

        return Math.min(total, Math.max(maxMinutes, 45));
    }

    private static String generateSessionSuggestion(String subjectName, int difficulty, int pendingTasks, boolean urgent) {
        if (urgent) {
            return "Focus on completing urgent tasks for " + subjectName;
        }

        switch (difficulty) {
            case 4:
                return subjectName + " requires intensive study. Break into smaller chunks.";
            case 3:
                return "Review key concepts in " + subjectName + " with practice problems.";
            case 2:
                return "Maintain progress in " + subjectName + " with regular review.";
            default:
                return "Quick review session for " + subjectName + " to reinforce learning.";
        }
    }

    private static List<ScheduleDay> generateWeeklySchedule(
            List<Subject> subjects, List<Task> tasks, List<SubjectPriority> priorities, int availableHours) {

        List<ScheduleDay> weeklySchedule = new ArrayList<>();
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        int totalAvailableMinutes = availableHours * 60;
        int subjectsCount = Math.max(1, subjects.size());

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            ScheduleDay day = new ScheduleDay(dayNames[dayIndex]);
            List<ScheduleSlot> slots = new ArrayList<>();

            int dayMinutes = totalAvailableMinutes;
            int slotStartHour = 8;

            for (SubjectPriority priority : priorities) {
                if (dayMinutes <= 0) break;

                Subject subject = findSubjectById(subjects, priority.getSubjectId());
                if (subject == null) continue;

                int minutesForSubject = Math.min(dayMinutes, Math.max(30, totalAvailableMinutes / subjectsCount));

                if (dayMinutes >= 30) {
                    String startTime = String.format("%02d:00", slotStartHour);
                    int endHour = slotStartHour + (minutesForSubject / 60);
                    int endMin = minutesForSubject % 60;
                    String endTime = String.format("%02d:%02d", endHour, endMin);

                    slots.add(new ScheduleSlot(
                            subject.getSubjectId(),
                            subject.getSubjectName(),
                            startTime,
                            endTime,
                            minutesForSubject,
                            subject.getColor()
                    ));

                    dayMinutes -= minutesForSubject;
                    slotStartHour = endHour + (endMin > 0 ? 1 : 0);
                }
            }

            day.setSlots(slots);
            weeklySchedule.add(day);
        }

        return weeklySchedule;
    }

    private static Subject findSubjectById(List<Subject> subjects, String subjectId) {
        for (Subject subject : subjects) {
            if (subject.getSubjectId().equals(subjectId)) {
                return subject;
            }
        }
        return null;
    }

    public static String detectOverloadedSchedule(List<Subject> subjects, List<Task> tasks, int availableHours) {
        int totalPendingTasks = 0;
        int urgentTasks = 0;

        for (Task task : tasks) {
            if (task.getStatus() != 2) {
                totalPendingTasks++;
                if (task.isDueThisWeek() || task.isOverdue()) {
                    urgentTasks++;
                }
            }
        }

        int totalMinutesNeeded = totalPendingTasks * 60;
        int totalMinutesAvailable = availableHours * 7;

        if (urgentTasks > availableHours * 2) {
            return "Your schedule is overloaded with " + urgentTasks + " urgent tasks. Consider extending deadlines or reducing scope.";
        }

        if (totalMinutesNeeded > totalMinutesAvailable * 1.5) {
            return "You may not have enough time to complete all tasks. Focus on high-priority items first.";
        }

        return null;
    }

    public static Subject findWeakestSubject(List<Subject> subjects, List<Task> tasks) {
        Subject weakest = null;
        float lowestScore = Float.MAX_VALUE;

        for (Subject subject : subjects) {
            int failedTasks = 0;
            int totalTasks = 0;

            for (Task task : tasks) {
                if (task.getSubjectId().equals(subject.getSubjectId())) {
                    totalTasks++;
                    if (task.getStatus() == 2 && task.isOverdue()) {
                        failedTasks++;
                    }
                }
            }

            float score = totalTasks > 0 ? (float) failedTasks / totalTasks : 0;
            float adjustedScore = score * subject.getDifficultyLevel();

            if (adjustedScore < lowestScore && totalTasks > 0) {
                lowestScore = adjustedScore;
                weakest = subject;
            }
        }

        return weakest;
    }
}
