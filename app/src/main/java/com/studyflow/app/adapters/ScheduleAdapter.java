package com.studyflow.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studyflow.app.R;
import com.studyflow.app.models.StudySchedule;
import com.studyflow.app.utils.ColorUtils;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private final List<StudySchedule.ScheduleDay> scheduleDays;

    public ScheduleAdapter(List<StudySchedule.ScheduleDay> scheduleDays) {
        this.scheduleDays = scheduleDays;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_day, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        StudySchedule.ScheduleDay day = scheduleDays.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return scheduleDays.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDayName;
        private final LinearLayout llSlots;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            llSlots = itemView.findViewById(R.id.llSlots);
        }

        void bind(StudySchedule.ScheduleDay day) {
            tvDayName.setText(day.getDayName());
            llSlots.removeAllViews();

            if (day.getSlots().isEmpty()) {
                TextView tvNoClass = new TextView(itemView.getContext());
                tvNoClass.setText("No study sessions scheduled");
                tvNoClass.setTextSize(13);
                tvNoClass.setTextColor(Color.parseColor("#9CA3AF"));
                tvNoClass.setPadding(0, 8, 0, 8);
                llSlots.addView(tvNoClass);
            } else {
                for (StudySchedule.ScheduleSlot slot : day.getSlots()) {
                    View slotView = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_schedule_slot, llSlots, false);

                    View slotColor = slotView.findViewById(R.id.slotColor);
                    TextView tvSubject = slotView.findViewById(R.id.tvSubject);
                    TextView tvTime = slotView.findViewById(R.id.tvTime);
                    TextView tvDuration = slotView.findViewById(R.id.tvDuration);

                    tvSubject.setText(slot.getSubjectName());
                    tvTime.setText(slot.getStartTime() + " - " + slot.getEndTime());
                    tvDuration.setText(slot.getDurationMinutes() + " min");

                    GradientDrawable colorBg = (GradientDrawable) slotColor.getBackground();
                    colorBg.setColor(ColorUtils.getSubjectColor(slot.getColor()));

                    llSlots.addView(slotView);
                }
            }
        }
    }
}
