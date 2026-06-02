package com.studyflow.app.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.studyflow.app.R;
import com.studyflow.app.models.Subject;
import com.studyflow.app.utils.ColorUtils;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final List<Subject> subjects;
    private final OnSubjectClickListener listener;

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
    }

    public SubjectAdapter(List<Subject> subjects, OnSubjectClickListener listener) {
        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final View colorIndicator;
        private final TextView tvSubjectName, tvDifficulty, tvTargetGrade;

        SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSubject);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvTargetGrade = itemView.findViewById(R.id.tvTargetGrade);
        }

        void bind(Subject subject) {
            tvSubjectName.setText(subject.getSubjectName());
            tvDifficulty.setText(subject.getDifficultyText());
            tvTargetGrade.setText("Target: " + (subject.getTargetGrade() != null ? subject.getTargetGrade() : "N/A"));

            GradientDrawable colorBg = (GradientDrawable) colorIndicator.getBackground();
            colorBg.setColor(ColorUtils.getSubjectColor(subject.getColor()));

            GradientDrawable diffBg = new GradientDrawable();
            diffBg.setShape(GradientDrawable.RECTANGLE);
            diffBg.setCornerRadius(8f);
            diffBg.setColor(ColorUtils.getDifficultyColor(subject.getDifficultyLevel()) | 0x20000000);
            tvDifficulty.setBackground(diffBg);
            tvDifficulty.setTextColor(ColorUtils.getDifficultyColor(subject.getDifficultyLevel()));

            cardView.setOnClickListener(v -> {
                if (listener != null) listener.onSubjectClick(subject);
            });
        }
    }
}
