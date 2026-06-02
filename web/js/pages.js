// Page renderers
const Pages = {
    subjects: [],
    tasks: [],
    sessions: [],

    async loadData() {
        if (!currentUser) return;
        try {
            this.subjects = await Database.getSubjects(currentUser.uid);
            this.tasks = await Database.getTasks(currentUser.uid);
            this.sessions = await Database.getSessions(currentUser.uid);
        } catch (e) {
            console.error('Error loading data:', e);
        }
    },

    // Dashboard
    renderDashboard() {
        const now = Date.now();
        const completedTasks = this.tasks.filter(t => t.status === 2).length;
        const totalSessions = this.sessions.length;
        const totalMinutes = this.sessions.reduce((sum, s) => sum + (s.durationMinutes || 0), 0);
        const streak = this.calculateStreak();

        const upcomingTasks = this.tasks
            .filter(t => t.status !== 2 && t.dueDate > now)
            .sort((a, b) => a.dueDate - b.dueDate)
            .slice(0, 5);

        const greeting = this.getGreeting();

        return `
            <div class="flex gap-16 mb-24" style="align-items:center;flex-wrap:wrap">
                <div style="flex:1;min-width:200px">
                    <div style="font-size:14px;color:var(--text-secondary)">${greeting}</div>
                    <div style="font-size:24px;font-weight:800" id="dashboard-user-name">${this.getUserName()}</div>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-card primary">
                    <div class="stat-icon"><i class="fas fa-clock"></i></div>
                    <div class="stat-value">${this.formatHours(totalMinutes)}</div>
                    <div class="stat-label">Study Hours</div>
                </div>
                <div class="stat-card secondary">
                    <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
                    <div class="stat-value">${completedTasks}</div>
                    <div class="stat-label">Tasks Done</div>
                </div>
                <div class="stat-card success">
                    <div class="stat-icon"><i class="fas fa-fire"></i></div>
                    <div class="stat-value">${streak}</div>
                    <div class="stat-label">Day Streak</div>
                </div>
                <div class="stat-card warning">
                    <div class="stat-icon"><i class="fas fa-book"></i></div>
                    <div class="stat-value">${this.subjects.length}</div>
                    <div class="stat-label">Subjects</div>
                </div>
            </div>

            <div class="section-header">
                <h2>Quick Actions</h2>
            </div>
            <div class="quick-actions">
                <div class="action-card" onclick="navigateTo('schedule')">
                    <i class="fas fa-robot" style="color:var(--primary)"></i>
                    <span>AI Schedule</span>
                </div>
                <div class="action-card" onclick="navigateTo('timer')">
                    <i class="fas fa-clock" style="color:var(--secondary)"></i>
                    <span>Focus Timer</span>
                </div>
                <div class="action-card" onclick="showSubjectModal()">
                    <i class="fas fa-plus-circle" style="color:var(--success)"></i>
                    <span>Add Subject</span>
                </div>
                <div class="action-card" onclick="showTaskModal()">
                    <i class="fas fa-plus-square" style="color:var(--warning)"></i>
                    <span>Add Task</span>
                </div>
            </div>

            <div class="section-header">
                <h2>Upcoming Deadlines</h2>
            </div>
            <div class="item-list">
                ${upcomingTasks.length > 0 ? upcomingTasks.map(t => this.renderTaskItem(t)).join('') :
                    '<div class="empty-state"><i class="fas fa-check-circle"></i><p>No upcoming deadlines</p></div>'}
            </div>
        `;
    },

    // Subjects
    renderSubjects() {
        return `
            <div class="section-header">
                <h2>My Subjects</h2>
                <button class="btn-primary" style="width:auto;padding:10px 20px" onclick="showSubjectModal()">
                    <i class="fas fa-plus"></i> Add Subject
                </button>
            </div>
            <div class="item-list">
                ${this.subjects.length > 0 ? this.subjects.map(s => `
                    <div class="list-item" onclick="showSubjectModal('${s.subjectId}')">
                        <div class="priority-dot" style="background:${s.color || '#6C63FF'}"></div>
                        <div class="item-content">
                            <div class="item-title">${s.subjectName}</div>
                            <div class="item-subtitle">${this.getDifficultyText(s.difficultyLevel)} · Target: ${s.targetGrade || 'N/A'}</div>
                        </div>
                        <div class="item-actions">
                            <button onclick="event.stopPropagation();deleteSubject('${s.subjectId}')" class="delete">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                `).join('') :
                    '<div class="empty-state"><i class="fas fa-book"></i><p>No subjects yet</p><button class="btn-primary" style="width:auto" onclick="showSubjectModal()">Add First Subject</button></div>'}
            </div>
        `;
    },

    // Tasks
    renderTasks(filter = 'all') {
        let filtered = [...this.tasks];
        if (filter === 'pending') filtered = filtered.filter(t => t.status === 0);
        else if (filter === 'progress') filtered = filtered.filter(t => t.status === 1);
        else if (filter === 'completed') filtered = filtered.filter(t => t.status === 2);

        filtered.sort((a, b) => a.dueDate - b.dueDate);

        return `
            <div class="section-header">
                <h2>My Tasks</h2>
                <button class="btn-primary" style="width:auto;padding:10px 20px" onclick="showTaskModal()">
                    <i class="fas fa-plus"></i> Add Task
                </button>
            </div>
            <div class="filter-bar">
                <button class="filter-chip ${filter === 'all' ? 'active' : ''}" onclick="renderPage('tasks', 'all')">All</button>
                <button class="filter-chip ${filter === 'pending' ? 'active' : ''}" onclick="renderPage('tasks', 'pending')">Pending</button>
                <button class="filter-chip ${filter === 'progress' ? 'active' : ''}" onclick="renderPage('tasks', 'progress')">In Progress</button>
                <button class="filter-chip ${filter === 'completed' ? 'active' : ''}" onclick="renderPage('tasks', 'completed')">Completed</button>
            </div>
            <div class="item-list">
                ${filtered.length > 0 ? filtered.map(t => this.renderTaskItem(t)).join('') :
                    '<div class="empty-state"><i class="fas fa-clipboard"></i><p>No tasks found</p><button class="btn-primary" style="width:auto" onclick="showTaskModal()">Add First Task</button></div>'}
            </div>
        `;
    },

    renderTaskItem(task) {
        const priority = task.priority === 1 ? 'high' : task.priority === 3 ? 'low' : 'medium';
        const statusClass = task.status === 2 ? 'completed' : task.status === 1 ? 'progress' : 'pending';
        const isOverdue = task.dueDate < Date.now() && task.status !== 2;
        const dueText = this.formatDueDate(task.dueDate);
        const subject = this.subjects.find(s => s.subjectId === task.subjectId);
        const typeColors = { assignment: '#6C63FF', quiz: '#FF6584', project: '#FFA502', exam: '#FF4757' };

        return `
            <div class="list-item ${priority}">
                <input type="checkbox" ${task.status === 2 ? 'checked' : ''}
                    onchange="toggleTaskStatus('${task.taskId}', this.checked)"
                    style="width:20px;height:20px;accent-color:var(--primary);cursor:pointer">
                <div class="item-content">
                    <div class="item-title" style="${task.status === 2 ? 'text-decoration:line-through;opacity:0.6' : ''}">${task.taskName}</div>
                    <div class="item-subtitle">
                        <span style="color:${typeColors[task.taskType] || '#6C63FF'};font-weight:600">${task.taskType || 'task'}</span>
                        ${subject ? ` · ${subject.subjectName}` : ''}
                    </div>
                </div>
                <span class="item-badge badge-${statusClass}">${this.getStatusText(task.status)}</span>
                <span style="font-size:12px;color:${isOverdue ? 'var(--error)' : 'var(--text-secondary)'};white-space:nowrap">${dueText}</span>
                <div class="item-actions">
                    <button onclick="event.stopPropagation();showTaskModal('${task.taskId}')"><i class="fas fa-edit"></i></button>
                    <button onclick="event.stopPropagation();deleteTask('${task.taskId}')" class="delete"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `;
    },

    // Schedule
    renderSchedule() {
        const settings = JSON.parse(localStorage.getItem('studyflow_settings') || '{}');
        const availableHours = settings.availableHours || 4;
        const result = ScheduleGenerator.generate(this.subjects, this.tasks, availableHours);
        const overload = ScheduleGenerator.detectOverload(this.subjects, this.tasks, availableHours);

        return `
            <div class="section-header">
                <h2>AI Study Schedule</h2>
            </div>

            <div class="card mb-24">
                <div class="card-body" style="background:linear-gradient(135deg, var(--primary), var(--primary-light));color:white;text-align:center">
                    <i class="fas fa-robot" style="font-size:32px;margin-bottom:12px"></i>
                    <h3 style="margin-bottom:4px">Generate AI Schedule</h3>
                    <p style="opacity:0.8;font-size:13px;margin-bottom:16px">Let AI plan your optimal study time</p>
                    <div class="flex gap-8" style="justify-content:center;align-items:center;flex-wrap:wrap">
                        <label style="font-size:13px">Hours/day:</label>
                        <input type="range" min="1" max="12" value="${availableHours}" id="hours-slider"
                            onchange="updateHours(this.value)" style="width:120px">
                        <span id="hours-display" style="font-weight:700">${availableHours}h</span>
                    </div>
                </div>
            </div>

            ${overload ? `<div style="background:rgba(255,165,2,0.1);color:var(--warning);padding:12px 16px;border-radius:var(--radius-sm);margin-bottom:16px;font-size:13px"><i class="fas fa-exclamation-triangle"></i> ${overload}</div>` : ''}

            <div class="section-header"><h2>Subject Priority</h2></div>
            <div class="mb-24">
                ${result.priorities.length > 0 ? result.priorities.map(p => `
                    <div class="priority-item">
                        <div class="priority-rank">#${p.rank}</div>
                        <div class="priority-dot" style="background:${p.color}"></div>
                        <div class="priority-info">
                            <div class="priority-name">${p.subjectName}</div>
                            <div class="priority-reason">${p.reason}</div>
                        </div>
                    </div>
                `).join('') : '<div class="empty-state"><p>Add subjects to generate schedule</p></div>'}
            </div>

            <div class="section-header"><h2>Weekly Schedule</h2></div>
            <div class="schedule-grid">
                ${result.schedule.map(day => `
                    <div class="schedule-day">
                        <div class="schedule-day-name">${day.day}</div>
                        ${day.slots.length > 0 ? day.slots.map(s => `
                            <div class="schedule-slot">
                                <div class="slot-bar" style="background:${s.color}"></div>
                                <div class="slot-info">
                                    <div class="slot-subject">${s.subjectName}</div>
                                    <div class="slot-time">${s.startTime} - ${s.endTime}</div>
                                </div>
                                <div class="slot-duration">${s.duration} min</div>
                            </div>
                        `).join('') : '<div style="font-size:12px;color:var(--text-hint);padding:8px 0">Rest day</div>'}
                    </div>
                `).join('')}
            </div>
        `;
    },

    // Timer
    renderTimer() {
        PomodoroTimer.init();
        return `
            <div class="timer-container">
                <div class="timer-ring">
                    <div class="timer-display">
                        <div class="timer-time" id="timer-time">25:00</div>
                        <div class="timer-status" id="timer-status">Focus Mode</div>
                    </div>
                </div>

                <div class="timer-controls">
                    <button class="btn-timer-secondary" onclick="PomodoroTimer.reset()" title="Reset">
                        <i class="fas fa-redo"></i>
                    </button>
                    <button class="btn-timer-main" id="btn-play" onclick="PomodoroTimer.start()" title="Start">
                        <i class="fas fa-play"></i>
                    </button>
                    <button class="btn-timer-main" id="btn-pause" onclick="PomodoroTimer.pause()" title="Pause" style="display:none">
                        <i class="fas fa-pause"></i>
                    </button>
                    <button class="btn-timer-secondary" onclick="PomodoroTimer.skip()" title="Skip">
                        <i class="fas fa-forward"></i>
                    </button>
                </div>

                <div class="timer-sessions">
                    <div class="count" id="timer-sessions-count">0</div>
                    <div class="label">Sessions Completed</div>
                </div>
            </div>
        `;
    },

    // Analytics
    renderAnalytics() {
        const totalMinutes = this.sessions.reduce((sum, s) => sum + (s.durationMinutes || 0), 0);
        const completedTasks = this.tasks.filter(t => t.status === 2).length;
        const totalTasks = this.tasks.length;
        const completionRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

        const typeCounts = { assignment: 0, quiz: 0, project: 0, exam: 0 };
        this.tasks.forEach(t => { if (typeCounts.hasOwnProperty(t.taskType)) typeCounts[t.taskType]++; });

        const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        const dailyMinutes = new Array(7).fill(0);
        this.sessions.forEach(s => {
            const d = new Date(s.studyDate || Date.now()).getDay();
            const idx = d === 0 ? 6 : d - 1;
            dailyMinutes[idx] += s.durationMinutes || 0;
        });
        const maxDaily = Math.max(...dailyMinutes, 1);

        return `
            <div class="section-header"><h2>Analytics</h2></div>

            <div class="stats-grid mb-24">
                <div class="stat-card primary">
                    <div class="stat-icon"><i class="fas fa-clock"></i></div>
                    <div class="stat-value">${this.formatHours(totalMinutes)}</div>
                    <div class="stat-label">Total Focus Time</div>
                </div>
                <div class="stat-card secondary">
                    <div class="stat-icon"><i class="fas fa-tasks"></i></div>
                    <div class="stat-value">${completionRate}%</div>
                    <div class="stat-label">Completion Rate</div>
                </div>
                <div class="stat-card success">
                    <div class="stat-icon"><i class="fas fa-check-double"></i></div>
                    <div class="stat-value">${completedTasks}/${totalTasks}</div>
                    <div class="stat-label">Tasks Done</div>
                </div>
                <div class="stat-card warning">
                    <div class="stat-icon"><i class="fas fa-stopwatch"></i></div>
                    <div class="stat-value">${this.sessions.length}</div>
                    <div class="stat-label">Total Sessions</div>
                </div>
            </div>

            <div class="chart-container mb-24">
                <h3>Weekly Study Hours</h3>
                <div class="bar-chart" style="margin-bottom:32px">
                    ${days.map((day, i) => `
                        <div class="bar" style="height:${(dailyMinutes[i] / maxDaily) * 100}%;background:var(--primary)">
                            <div class="bar-value">${dailyMinutes[i]}m</div>
                            <div class="bar-label">${day}</div>
                        </div>
                    `).join('')}
                </div>
            </div>

            <div class="chart-container">
                <h3>Task Distribution</h3>
                <div class="bar-chart" style="margin-bottom:32px;height:120px">
                    ${Object.entries(typeCounts).map(([type, count]) => {
                        const colors = { assignment: '#6C63FF', quiz: '#FF6584', project: '#FFA502', exam: '#FF4757' };
                        const max = Math.max(...Object.values(typeCounts), 1);
                        return `
                            <div class="bar" style="height:${(count / max) * 100}%;background:${colors[type]}">
                                <div class="bar-value">${count}</div>
                                <div class="bar-label">${type}</div>
                            </div>
                        `;
                    }).join('')}
                </div>
            </div>
        `;
    },

    // Helpers
    getUserName() {
        const user = JSON.parse(localStorage.getItem('studyflow_user') || '{}');
        return user.name || currentUser?.email?.split('@')[0] || 'Student';
    },

    getGreeting() {
        const hour = new Date().getHours();
        if (hour < 12) return 'Good Morning';
        if (hour < 17) return 'Good Afternoon';
        return 'Good Evening';
    },

    formatHours(minutes) {
        if (minutes < 60) return `${minutes}m`;
        return `${(minutes / 60).toFixed(1)}h`;
    },

    formatDueDate(timestamp) {
        if (!timestamp) return '';
        const diff = timestamp - Date.now();
        if (diff < 0) return 'Overdue';
        const days = Math.floor(diff / 86400000);
        if (days > 0) return `${days}d left`;
        const hours = Math.floor(diff / 3600000);
        if (hours > 0) return `${hours}h left`;
        return `${Math.floor(diff / 60000)}m left`;
    },

    getDifficultyText(level) {
        return ['', 'Easy', 'Medium', 'Hard', 'Very Hard'][level] || 'Medium';
    },

    getStatusText(status) {
        return ['Pending', 'In Progress', 'Completed'][status] || 'Pending';
    },

    calculateStreak() {
        const dates = [...new Set(this.sessions.map(s => {
            const d = new Date(s.studyDate || Date.now());
            return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
        }))].sort().reverse();

        if (dates.length === 0) return 0;

        let streak = 0;
        let checkDate = new Date();
        checkDate.setHours(0, 0, 0, 0);

        for (const dateStr of dates) {
            const d = new Date(checkDate);
            const dateStr2 = `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
            if (dateStr === dateStr2) {
                streak++;
                checkDate.setDate(checkDate.getDate() - 1);
            } else {
                break;
            }
        }

        return streak;
    }
};
