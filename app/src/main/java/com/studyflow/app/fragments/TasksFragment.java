package com.studyflow.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studyflow.app.R;
import com.studyflow.app.activities.AddEditTaskActivity;
import com.studyflow.app.adapters.TaskAdapter;
import com.studyflow.app.models.Task;
import com.studyflow.app.models.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView rvTasks;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private Chip chipAll, chipPending, chipInProgress, chipCompleted;
    private TaskAdapter adapter;
    private List<Task> allTasks;
    private List<Task> filteredTasks;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private int currentFilter = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupClickListeners();
        loadTasks();
    }

    private void initViews(View view) {
        rvTasks = view.findViewById(R.id.rvTasks);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipInProgress = view.findViewById(R.id.chipInProgress);
        chipCompleted = view.findViewById(R.id.chipCompleted);
    }

    private void setupFirestore() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        allTasks = new ArrayList<>();
        filteredTasks = new ArrayList<>();
        adapter = new TaskAdapter(filteredTasks, task -> {
            Intent intent = new Intent(requireContext(), AddEditTaskActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditTaskActivity.class)));

        chipAll.setOnClickListener(v -> filterTasks(0));
        chipPending.setOnClickListener(v -> filterTasks(1));
        chipInProgress.setOnClickListener(v -> filterTasks(2));
        chipCompleted.setOnClickListener(v -> filterTasks(3));
    }

    private void filterTasks(int filter) {
        currentFilter = filter;
        filteredTasks.clear();

        for (Task task : allTasks) {
            if (filter == 0 ||
                    (filter == 1 && task.getStatus() == 0) ||
                    (filter == 2 && task.getStatus() == 1) ||
                    (filter == 3 && task.getStatus() == 2)) {
                filteredTasks.add(task);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
        rvTasks.setVisibility(filteredTasks.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void loadTasks() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        firestore.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allTasks.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Task task = doc.toObject(Task.class);
                        allTasks.add(task);
                    }
                    Collections.sort(allTasks, (a, b) -> Long.compare(a.getDueDate(), b.getDueDate()));
                    filterTasks(currentFilter);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }
}
