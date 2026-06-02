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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studyflow.app.R;
import com.studyflow.app.activities.AddEditSubjectActivity;
import com.studyflow.app.adapters.SubjectAdapter;
import com.studyflow.app.models.Subject;
import com.studyflow.app.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class SubjectsFragment extends Fragment {

    private RecyclerView rvSubjects;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private SubjectAdapter adapter;
    private List<Subject> subjects;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subjects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupClickListeners();
        loadSubjects();
    }

    private void initViews(View view) {
        rvSubjects = view.findViewById(R.id.rvSubjects);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void setupFirestore() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        subjects = new ArrayList<>();
        adapter = new SubjectAdapter(subjects, subject -> {
            Intent intent = new Intent(requireContext(), AddEditSubjectActivity.class);
            intent.putExtra("subject", subject);
            startActivity(intent);
        });
        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubjects.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditSubjectActivity.class)));
    }

    private void loadSubjects() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        firestore.collection("subjects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    subjects.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subject subject = doc.toObject(Subject.class);
                        subjects.add(subject);
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(subjects.isEmpty() ? View.VISIBLE : View.GONE);
                    rvSubjects.setVisibility(subjects.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSubjects();
    }
}
