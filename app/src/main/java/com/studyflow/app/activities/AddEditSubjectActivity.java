package com.studyflow.app.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studyflow.app.R;
import com.studyflow.app.models.Subject;
import com.studyflow.app.utils.ColorUtils;

import java.util.UUID;

public class AddEditSubjectActivity extends AppCompatActivity {

    private EditText etSubjectName, etTargetGrade;
    private Spinner spinnerDifficulty;
    private Button btnSave, btnDelete;
    private TextView tvTitle;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Subject editSubject;
    private boolean isEditMode = false;
    private int selectedColorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_subject);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupSpinners();
        checkEditMode();
        setupClickListeners();
    }

    private void initViews() {
        etSubjectName = findViewById(R.id.etSubjectName);
        etTargetGrade = findViewById(R.id.etTargetGrade);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        tvTitle = findViewById(R.id.tvTitle);
    }

    private void setupSpinners() {
        String[] difficulties = {"Easy", "Medium", "Hard", "Very Hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);
        spinnerDifficulty.setSelection(1);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("subject")) {
            editSubject = (Subject) getIntent().getSerializableExtra("subject");
            if (editSubject != null) {
                isEditMode = true;
                tvTitle.setText(R.string.edit_subject);
                etSubjectName.setText(editSubject.getSubjectName());
                etTargetGrade.setText(editSubject.getTargetGrade());
                spinnerDifficulty.setSelection(editSubject.getDifficultyLevel() - 1);
                btnDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveSubject());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Subject")
                    .setMessage("Are you sure you want to delete this subject?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteSubject())
                    .setNegativeButton("No", null)
                    .show();
        });

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void saveSubject() {
        String name = etSubjectName.getText().toString().trim();
        String targetGrade = etTargetGrade.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etSubjectName.setError("Subject name is required");
            etSubjectName.requestFocus();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        int difficulty = spinnerDifficulty.getSelectedItemPosition() + 1;
        String color = ColorUtils.colorToHex(ColorUtils.getSubjectColor(selectedColorIndex));

        if (isEditMode && editSubject != null) {
            editSubject.setSubjectName(name);
            editSubject.setDifficultyLevel(difficulty);
            editSubject.setTargetGrade(targetGrade);
            editSubject.setColor(color);

            firestore.collection("subjects").document(editSubject.getSubjectId())
                    .update(editSubject.toMap())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Subject updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            String subjectId = UUID.randomUUID().toString();
            Subject subject = new Subject(subjectId, userId, name, difficulty, targetGrade, color);

            firestore.collection("subjects").document(subjectId)
                    .set(subject.toMap())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Subject added!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteSubject() {
        if (editSubject == null) return;

        firestore.collection("subjects").document(editSubject.getSubjectId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Subject deleted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
