package com.studyflow.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studyflow.app.R;
import com.studyflow.app.models.Subject;
import com.studyflow.app.models.Task;
import com.studyflow.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AddEditTaskActivity extends AppCompatActivity {

    private EditText etTaskName;
    private Spinner spinnerSubject, spinnerType, spinnerPriority;
    private TextView tvDueDate;
    private Button btnSave, btnDelete;
    private TextView tvTitle;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Task editTask;
    private boolean isEditMode = false;
    private long selectedDueDate = 0;
    private List<Subject> subjectList;
    private List<String> subjectNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        subjectList = new ArrayList<>();
        subjectNames = new ArrayList<>();

        initViews();
        setupSpinners();
        loadSubjects();
        checkEditMode();
        setupClickListeners();
    }

    private void initViews() {
        etTaskName = findViewById(R.id.etTaskName);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        tvDueDate = findViewById(R.id.tvDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        tvTitle = findViewById(R.id.tvTitle);
    }

    private void setupSpinners() {
        String[] types = {"Assignment", "Quiz", "Project", "Exam"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1);
    }

    private void loadSubjects() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        firestore.collection("subjects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    subjectList.clear();
                    subjectNames.clear();
                    subjectNames.add("Select Subject");

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subject subject = doc.toObject(Subject.class);
                        subjectList.add(subject);
                        subjectNames.add(subject.getSubjectName());
                    }

                    ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, subjectNames);
                    subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSubject.setAdapter(subjectAdapter);

                    if (isEditMode && editTask != null) {
                        for (int i = 0; i < subjectList.size(); i++) {
                            if (subjectList.get(i).getSubjectId().equals(editTask.getSubjectId())) {
                                spinnerSubject.setSelection(i + 1);
                                break;
                            }
                        }
                    }
                });
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("task")) {
            editTask = (Task) getIntent().getSerializableExtra("task");
            if (editTask != null) {
                isEditMode = true;
                tvTitle.setText(R.string.edit_task);
                etTaskName.setText(editTask.getTaskName());
                selectedDueDate = editTask.getDueDate();
                tvDueDate.setText(DateUtils.formatDate(selectedDueDate));

                String[] types = {"Assignment", "Quiz", "Project", "Exam"};
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase(editTask.getTaskType())) {
                        spinnerType.setSelection(i);
                        break;
                    }
                }

                spinnerPriority.setSelection(editTask.getPriority() - 1);
                btnDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveTask());

        btnDelete.setOnClickListener(v -> deleteTask());

        tvDueDate.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 23, 59, 59);
            selectedDueDate = selected.getTimeInMillis();
            tvDueDate.setText(DateUtils.formatDate(selectedDueDate));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String name = etTaskName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etTaskName.setError("Task name is required");
            etTaskName.requestFocus();
            return;
        }

        if (spinnerSubject.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDueDate == 0) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        String subjectId = subjectList.get(spinnerSubject.getSelectedItemPosition() - 1).getSubjectId();
        String type = spinnerType.getSelectedItem().toString().toLowerCase();
        int priority = spinnerPriority.getSelectedItemPosition() + 1;

        if (isEditMode && editTask != null) {
            editTask.setTaskName(name);
            editTask.setSubjectId(subjectId);
            editTask.setTaskType(type);
            editTask.setDueDate(selectedDueDate);
            editTask.setPriority(priority);

            firestore.collection("tasks").document(editTask.getTaskId())
                    .update(editTask.toMap())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            String taskId = UUID.randomUUID().toString();
            Task task = new Task(taskId, subjectId, userId, name, type, selectedDueDate, priority, 0);

            firestore.collection("tasks").document(taskId)
                    .set(task.toMap())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteTask() {
        if (editTask == null) return;

        firestore.collection("tasks").document(editTask.getTaskId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
