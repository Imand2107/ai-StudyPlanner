package com.studyflow.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studyflow.app.R;
import com.studyflow.app.activities.AddEditSubjectActivity;
import com.studyflow.app.activities.AddEditTaskActivity;
import com.studyflow.app.activities.ProfileActivity;
import com.studyflow.app.activities.ScheduleGeneratorActivity;
import com.studyflow.app.activities.FocusTimerActivity;
import com.studyflow.app.adapters.TaskAdapter;
import com.studyflow.app.models.Task;
import com.studyflow.app.models.Subject;
import com.studyflow.app.utils.DateUtils;
import com.studyflow.app.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvGreeting, tvUserName, tvTotalHours, tvTasksCompleted, tvStreak;
    private RecyclerView rvUpcomingTasks;
    private CardView cvGenerateSchedule, cvStartFocus, cvAddSubject, cvAddTask;
    private LinearLayout llProfile;
    private TaskAdapter taskAdapter;
    private List<Task> upcomingTasks;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvTotalHours = view.findViewById(R.id.tvTotalHours);
        tvTasksCompleted = view.findViewById(R.id.tvTasksCompleted);
        tvStreak = view.findViewById(R.id.tvStreak);
        rvUpcomingTasks = view.findViewById(R.id.rvUpcomingTasks);
        cvGenerateSchedule = view.findViewById(R.id.cvGenerateSchedule);
        cvStartFocus = view.findViewById(R.id.cvStartFocus);
        cvAddSubject = view.findViewById(R.id.cvAddSubject);
        cvAddTask = view.findViewById(R.id.cvAddTask);
        llProfile = view.findViewById(R.id.llProfile);
    }

    private void setupFirestore() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        prefManager = new PreferenceManager(requireContext());
    }

    private void setupRecyclerView() {
        upcomingTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(upcomingTasks, null);
        rvUpcomingTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUpcomingTasks.setAdapter(taskAdapter);
    }

    private void setupClickListeners() {
        cvGenerateSchedule.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ScheduleGeneratorActivity.class)));

        cvStartFocus.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FocusTimerActivity.class)));

        cvAddSubject.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditSubjectActivity.class)));

        cvAddTask.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditTaskActivity.class)));

        llProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));
    }

    private void loadData() {
        updateGreeting();
        tvUserName.setText(prefManager.getUserName());
        tvStreak.setText(String.valueOf(prefManager.getStudyStreak()));

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        firestore.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int completed = 0;
                    List<Task> allTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Task task = doc.toObject(Task.class);
                        allTasks.add(task);
                        if (task.getStatus() == 2) {
                            completed++;
                        }
                    }
                    tvTasksCompleted.setText(String.valueOf(completed));

                    Collections.sort(allTasks, (a, b) -> Long.compare(a.getDueDate(), b.getDueDate()));

                    upcomingTasks.clear();
                    int count = 0;
                    for (Task task : allTasks) {
                        if (task.getStatus() != 2 && count < 5) {
                            upcomingTasks.add(task);
                            count++;
                        }
                    }
                    taskAdapter.notifyDataSetChanged();

                    int totalMinutes = 0;
                    long today = System.currentTimeMillis();
                    long weekAgo = today - 604800000L;
                    for (Task task : allTasks) {
                        if (task.getStatus() == 2 && task.getCreatedAt() > weekAgo) {
                            totalMinutes += 30;
                        }
                    }
                    tvTotalHours.setText(DateUtils.formatHours(totalMinutes / 60f));
                });

        firestore.collection("study_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalMinutes = 0;
                    long today = System.currentTimeMillis();
                    long weekAgo = today - 604800000L;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Long duration = doc.getLong("durationMinutes");
                        Long studyDate = doc.getLong("studyDate");
                        if (duration != null && studyDate != null && studyDate > weekAgo) {
                            totalMinutes += duration.intValue();
                        }
                    }
                    tvTotalHours.setText(DateUtils.formatHours(totalMinutes / 60f));
                });
    }

    private void updateGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText(R.string.good_morning);
        } else if (hour < 17) {
            tvGreeting.setText(R.string.good_afternoon);
        } else {
            tvGreeting.setText(R.string.good_evening);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
