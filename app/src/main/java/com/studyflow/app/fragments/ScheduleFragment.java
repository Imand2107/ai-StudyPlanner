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
import com.studyflow.app.activities.ScheduleGeneratorActivity;
import com.studyflow.app.adapters.ScheduleAdapter;
import com.studyflow.app.models.Subject;
import com.studyflow.app.models.Task;
import com.studyflow.app.models.StudySchedule;
import com.studyflow.app.ai.ScheduleGenerator;
import com.studyflow.app.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private TextView tvEmpty, tvScheduleInfo;
    private RecyclerView rvSchedule;
    private CardView cvGenerate;
    private LinearLayout llPriorities;
    private ScheduleAdapter adapter;
    private List<StudySchedule.ScheduleDay> scheduleDays;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupClickListeners();
        loadSchedule();
    }

    private void initViews(View view) {
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvScheduleInfo = view.findViewById(R.id.tvScheduleInfo);
        rvSchedule = view.findViewById(R.id.rvSchedule);
        cvGenerate = view.findViewById(R.id.cvGenerate);
        llPriorities = view.findViewById(R.id.llPriorities);
    }

    private void setupFirestore() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        prefManager = new PreferenceManager(requireContext());
    }

    private void setupRecyclerView() {
        scheduleDays = new ArrayList<>();
        adapter = new ScheduleAdapter(scheduleDays);
        rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedule.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cvGenerate.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ScheduleGeneratorActivity.class)));
    }

    private void loadSchedule() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        List<Subject> subjects = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();

        firestore.collection("subjects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(subjectSnapshot -> {
                    for (QueryDocumentSnapshot doc : subjectSnapshot) {
                        subjects.add(doc.toObject(Subject.class));
                    }

                    firestore.collection("tasks")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(taskSnapshot -> {
                                for (QueryDocumentSnapshot doc : taskSnapshot) {
                                    tasks.add(doc.toObject(Task.class));
                                }

                                if (!subjects.isEmpty()) {
                                    StudySchedule schedule = ScheduleGenerator.generateSchedule(
                                            subjects, tasks, prefManager.getAvailableHours());

                                    scheduleDays.clear();
                                    scheduleDays.addAll(schedule.getWeeklySchedule());
                                    adapter.notifyDataSetChanged();

                                    updatePriorityDisplay(schedule.getSubjectPriorities());

                                    tvEmpty.setVisibility(View.GONE);
                                    rvSchedule.setVisibility(View.VISIBLE);
                                    tvScheduleInfo.setVisibility(View.VISIBLE);
                                } else {
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    rvSchedule.setVisibility(View.GONE);
                                    tvScheduleInfo.setVisibility(View.GONE);
                                }
                            });
                });
    }

    private void updatePriorityDisplay(List<StudySchedule.SubjectPriority> priorities) {
        llPriorities.removeAllViews();
        for (StudySchedule.SubjectPriority priority : priorities) {
            View itemView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_priority, llPriorities, false);

            TextView tvRank = itemView.findViewById(R.id.tvRank);
            TextView tvSubject = itemView.findViewById(R.id.tvSubject);
            TextView tvReason = itemView.findViewById(R.id.tvReason);
            TextView tvHours = itemView.findViewById(R.id.tvHours);

            tvRank.setText("#" + priority.getPriorityRank());
            tvSubject.setText(priority.getSubjectName());
            tvReason.setText(priority.getReason());
            tvHours.setText(String.format("%.1f hr/week", priority.getRecommendedHours()));

            llPriorities.addView(itemView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchedule();
    }
}
