package com.studyflow.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.studyflow.app.R;
import com.studyflow.app.models.Task;
import com.studyflow.app.utils.ColorUtils;
import com.studyflow.app.utils.DateUtils;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final View priorityIndicator;
        private final TextView tvTaskName, tvSubject, tvDueDate, tvPriority, tvType;
        private final CheckBox cbStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTask);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvType = itemView.findViewById(R.id.tvType);
            cbStatus = itemView.findViewById(R.id.cbStatus);
        }

        void bind(Task task) {
            tvTaskName.setText(task.getTaskName());
            tvDueDate.setText(DateUtils.getTimeRemaining(task.getDueDate()));
            tvPriority.setText(task.getPriorityText());
            tvType.setText(task.getTaskType() != null ? task.getTaskType() : "Task");

            GradientDrawable priorityBg = (GradientDrawable) priorityIndicator.getBackground();
            priorityBg.setColor(ColorUtils.getPriorityColor(task.getPriority()));

            int typeColor = ColorUtils.getTypeColor(task.getTaskType());
            GradientDrawable typeBg = new GradientDrawable();
            typeBg.setShape(GradientDrawable.RECTANGLE);
            typeBg.setCornerRadius(8f);
            typeBg.setColor(typeColor | 0x20000000);
            tvType.setBackground(typeBg);
            tvType.setTextColor(typeColor);

            cbStatus.setChecked(task.getStatus() == 2);
            cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setStatus(isChecked ? 2 : 0);
            });

            if (task.isOverdue()) {
                tvDueDate.setTextColor(Color.RED);
            } else if (task.isDueToday()) {
                tvDueDate.setTextColor(Color.parseColor("#FFA502"));
            } else {
                tvDueDate.setTextColor(Color.parseColor("#6B7280"));
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) listener.onTaskClick(task);
            });
        }
    }
}
