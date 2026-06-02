// AI Schedule Generator
const ScheduleGenerator = {
    generate(subjects, tasks, availableHours) {
        const priorities = this.calculatePriorities(subjects, tasks);
        const schedule = this.buildWeeklySchedule(subjects, tasks, priorities, availableHours);
        const recommendations = this.generateRecommendations(subjects, tasks, availableHours);

        return { priorities, schedule, recommendations };
    },

    calculatePriorities(subjects, tasks) {
        return subjects.map(subject => {
            const subjectTasks = tasks.filter(t => t.subjectId === subject.subjectId && t.status !== 2);
            const urgentTasks = subjectTasks.filter(t => this.isDueThisWeek(t.dueDate)).length;
            const pendingCount = subjectTasks.length;

            const score = (subject.difficultyLevel || 2) * 2.5 + urgentTasks * 3 + pendingCount * 1.5;

            let reason = '';
            if (urgentTasks > 0) reason += `${urgentTasks} urgent task(s)`;
            if (subject.difficultyLevel >= 3) reason += reason ? ' & High difficulty' : 'High difficulty';
            if (pendingCount > 3) reason += reason ? ' & Heavy workload' : 'Heavy workload';
            if (!reason) reason = 'Maintain consistent study';

            return {
                subjectId: subject.subjectId,
                subjectName: subject.subjectName,
                score,
                reason,
                color: subject.color || '#6C63FF',
                pendingCount,
                urgentTasks
            };
        }).sort((a, b) => b.score - a.score).map((p, i) => ({ ...p, rank: i + 1 }));
    },

    buildWeeklySchedule(subjects, tasks, priorities, availableHours) {
        const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
        const minutesPerDay = availableHours * 60;

        return days.map(day => {
            const slots = [];
            let remainingMinutes = minutesPerDay;
            let startHour = 8;

            priorities.forEach(priority => {
                if (remainingMinutes <= 0) return;

                const subject = subjects.find(s => s.subjectId === priority.subjectId);
                if (!subject) return;

                const duration = Math.min(remainingMinutes, Math.max(30, minutesPerDay / Math.max(1, priorities.length)));

                if (remainingMinutes >= 30) {
                    const start = `${String(startHour).padStart(2, '0')}:00`;
                    const endHour = startHour + Math.floor(duration / 60);
                    const endMin = duration % 60;
                    const end = `${String(endHour).padStart(2, '0')}:${String(endMin).padStart(2, '0')}`;

                    slots.push({
                        subjectId: subject.subjectId,
                        subjectName: subject.subjectName,
                        startTime: start,
                        endTime: end,
                        duration,
                        color: subject.color || '#6C63FF'
                    });

                    remainingMinutes -= duration;
                    startHour = endHour + (endMin > 0 ? 1 : 0);
                }
            });

            return { day, slots };
        });
    },

    generateRecommendations(subjects, tasks, availableHours) {
        return subjects.map(subject => {
            const subjectTasks = tasks.filter(t => t.subjectId === subject.subjectId && t.status !== 2);
            const hasUrgent = subjectTasks.some(t => this.isDueThisWeek(t.dueDate) || this.isOverdue(t.dueDate));

            let minutes = 30 + (subject.difficultyLevel || 2) * 10 + subjectTasks.length * 5;
            if (hasUrgent) minutes += 15;
            minutes = Math.min(minutes, (availableHours * 60) / Math.max(1, subjectTasks.length));

            let suggestion = '';
            if (hasUrgent) {
                suggestion = `Focus on completing urgent tasks for ${subject.subjectName}`;
            } else if (subject.difficultyLevel >= 3) {
                suggestion = `${subject.subjectName} requires intensive study. Break into smaller chunks.`;
            } else if (subject.difficultyLevel === 2) {
                suggestion = `Review key concepts in ${subject.subjectName} with practice problems.`;
            } else {
                suggestion = `Quick review session for ${subject.subjectName} to reinforce learning.`;
            }

            return {
                subjectId: subject.subjectId,
                subjectName: subject.subjectName,
                minutes: Math.round(minutes),
                suggestion,
                bestTime: hasUrgent ? 'Morning' : 'Afternoon',
                color: subject.color || '#6C63FF'
            };
        }).sort((a, b) => b.minutes - a.minutes);
    },

    isDueThisWeek(dueDate) {
        const now = Date.now();
        const weekEnd = now + 7 * 24 * 60 * 60 * 1000;
        return dueDate >= now && dueDate < weekEnd;
    },

    isOverdue(dueDate) {
        return dueDate < Date.now();
    },

    detectOverload(subjects, tasks, availableHours) {
        const pending = tasks.filter(t => t.status !== 2).length;
        const urgent = tasks.filter(t => t.status !== 2 && (this.isDueThisWeek(t.dueDate) || this.isOverdue(t.dueDate))).length;

        if (urgent > availableHours * 2) {
            return `Schedule overloaded with ${urgent} urgent tasks. Consider extending deadlines.`;
        }

        if (pending * 60 > availableHours * 7 * 1.5) {
            return 'May not have enough time for all tasks. Focus on high-priority items first.';
        }

        return null;
    }
};
