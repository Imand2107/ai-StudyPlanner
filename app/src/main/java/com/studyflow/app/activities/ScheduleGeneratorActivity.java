package com.studyflow.app.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studyflow.app.R;
import com.studyflow.app.ai.ScheduleGenerator;
import com.studyflow.app.models.Subject;
import com.studyflow.app.models.Task;
import com.studyflow.app.models.StudySchedule;
import com.studyflow.app.utils.ColorUtils;
import com.studyflow.app.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class ScheduleGeneratorActivity extends AppCompatActivity {

    private SeekBar seekBarHours;
    private TextView tvHoursValue, tvWarning;
    private LinearLayout llSchedule, llPriorities;
    private Button btnGenerate;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private PreferenceManager prefManager;
    private List<Subject> subjects;
    private List<Task> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_generator);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        prefManager = new PreferenceManager(this);
        subjects = new ArrayList<>();
        tasks = new ArrayList<>();

        initViews();
        setupSeekBar();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        seekBarHours = findViewById(R.id.seekBarHours);
        tvHoursValue = findViewById(R.id.tvHoursValue);
        tvWarning = findViewById(R.id.tvWarning);
        llSchedule = findViewById(R.id.llSchedule);
        llPriorities = findViewById(R.id.llPriorities);
        btnGenerate = findViewById(R.id.btnGenerate);

        seekBarHours.setProgress(prefManager.getAvailableHours() - 1);
        tvHoursValue.setText(prefManager.getAvailableHours() + " hours");
    }

    private void setupSeekBar() {
        seekBarHours.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int hours = progress + 1;
                tvHoursValue.setText(hours + " hours");
                prefManager.setAvailableHours(hours);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupClickListeners() {
        btnGenerate.setOnClickListener(v -> generateSchedule());
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void loadData() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

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
                            });
                });
    }

    private void generateSchedule() {
        if (subjects.isEmpty()) {
            tvWarning.setVisibility(View.VISIBLE);
            tvWarning.setText("Please add subjects first before generating a schedule.");
            return;
        }

        tvWarning.setVisibility(View.GONE);
        int availableHours = seekBarHours.getProgress() + 1;

        StudySchedule schedule = ScheduleGenerator.generateSchedule(subjects, tasks, availableHours);

        displaySchedule(schedule);
        displayPriorities(schedule.getSubjectPriorities());

        String overloaded = ScheduleGenerator.detectOverloadedSchedule(subjects, tasks, availableHours);
        if (overloaded != null) {
            tvWarning.setVisibility(View.VISIBLE);
            tvWarning.setText(overloaded);
        }
    }

    private void displaySchedule(StudySchedule schedule) {
        llSchedule.removeAllViews();

        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] dayIndices = {0, 1, 2, 3, 4, 5, 6};

        for (int dayIndex : dayIndices) {
            if (dayIndex < schedule.getWeeklySchedule().size()) {
                StudySchedule.ScheduleDay day = schedule.getWeeklySchedule().get(dayIndex);

                View dayView = LayoutInflater.from(this)
                        .inflate(R.layout.item_schedule_day_simple, llSchedule, false);

                TextView tvDay = dayView.findViewById(R.id.tvDay);
                LinearLayout llSlots = dayView.findViewById(R.id.llSlots);

                tvDay.setText(dayNames[dayIndex]);

                for (StudySchedule.ScheduleSlot slot : day.getSlots()) {
                    View slotView = LayoutInflater.from(this)
                            .inflate(R.layout.item_schedule_slot_simple, llSlots, false);

                    View colorBar = slotView.findViewById(R.id.colorBar);
                    TextView tvSubjectName = slotView.findViewById(R.id.tvSubjectName);
                    TextView tvTime = slotView.findViewById(R.id.tvTime);

                    tvSubjectName.setText(slot.getSubjectName());
                    tvTime.setText(slot.getStartTime() + " - " + slot.getEndTime());

                    GradientDrawable colorBg = (GradientDrawable) colorBar.getBackground();
                    colorBg.setColor(ColorUtils.getSubjectColor(slot.getColor()));

                    llSlots.addView(slotView);
                }

                if (day.getSlots().isEmpty()) {
                    TextView tvEmpty = new TextView(this);
                    tvEmpty.setText("Rest day");
                    tvEmpty.setTextSize(12);
                    tvEmpty.setTextColor(Color.parseColor("#9CA3AF"));
                    tvEmpty.setPadding(16, 8, 16, 8);
                    llSlots.addView(tvEmpty);
                }

                llSchedule.addView(dayView);
            }
        }
    }

    private void displayPriorities(List<StudySchedule.SubjectPriority> priorities) {
        llPriorities.removeAllViews();

        for (StudySchedule.SubjectPriority priority : priorities) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_priority_detail, llPriorities, false);

            TextView tvRank = itemView.findViewById(R.id.tvRank);
            TextView tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            TextView tvReason = itemView.findViewById(R.id.tvReason);
            TextView tvHours = itemView.findViewById(R.id.tvHours);
            View colorDot = itemView.findViewById(R.id.colorDot);

            tvRank.setText("#" + priority.getPriorityRank());
            tvSubjectName.setText(priority.getSubjectName());
            tvReason.setText(priority.getReason());
            tvHours.setText(String.format("%.1f hr/week", priority.getRecommendedHours()));

            GradientDrawable dotBg = (GradientDrawable) colorDot.getBackground();
            dotBg.setColor(ColorUtils.getSubjectColor(priority.getColor()));

            llPriorities.addView(itemView);
        }
    }
}
