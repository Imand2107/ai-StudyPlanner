// Page renderers – Premium StudyFlow
const Pages = {
    subjects: [],
    tasks: [],
    sessions: [],

    async loadData() {
        if (!currentUser) return;
        this.subjects = await Database.getSubjects(currentUser.uid);
        this.tasks = await Database.getTasks(currentUser.uid);
        this.sessions = await Database.getSessions(currentUser.uid);
    },

    getUserName() {
        const u = JSON.parse(localStorage.getItem('studyflow_user') || '{}');
        return u.name || currentUser?.email?.split('@')[0] || 'Student';
    },

    getGreeting() {
        const h = new Date().getHours();
        if (h < 12) return 'Good Morning';
        if (h < 17) return 'Good Afternoon';
        return 'Good Evening';
    },

    getMotivationalQuote() {
        const quotes = [
            "Small progress is still progress.",
            "The secret of getting ahead is getting started.",
            "Focus on being productive instead of busy.",
            "Success is the sum of small efforts repeated daily.",
            "Don't watch the clock; do what it does. Keep going.",
            "Your future is created by what you do today.",
            "Study hard, for the well is deep.",
            "Discipline is the bridge between goals and accomplishment."
        ];
        return quotes[Math.floor(Math.random() * quotes.length)];
    },

    formatMinutes(min) {
        if (min < 60) return `${min}m`;
        return `${(min / 60).toFixed(1)}h`;
    },

    formatDueDate(ts) {
        if (!ts) return '';
        const diff = ts - Date.now();
        if (diff < 0) return 'Overdue';
        const d = Math.floor(diff / 86400000);
        if (d > 0) return `${d}d left`;
        const h = Math.floor(diff / 3600000);
        if (h > 0) return `${h}h left`;
        return `${Math.floor(diff / 60000)}m left`;
    },

    getDifficultyText(l) { return ['', 'Easy', 'Medium', 'Hard', 'Very Hard'][l] || 'Medium'; },
    getStatusText(s) { return ['To Do', 'In Progress', 'Done'][s] || 'To Do'; },

    calculateStreak() {
        const dates = [...new Set(this.sessions.map(s => {
            const d = new Date(s.studyDate || Date.now());
            return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
        }))].sort().reverse();
        if (!dates.length) return 0;
        let streak = 0, check = new Date(); check.setHours(0, 0, 0, 0);
        for (const ds of dates) {
            const d2 = `${check.getFullYear()}-${check.getMonth()}-${check.getDate()}`;
            if (ds === d2) { streak++; check.setDate(check.getDate() - 1); } else break;
        }
        return streak;
    },

    getXP() {
        return (this.sessions.length * 25) + (this.tasks.filter(t => t.status === 2).length * 15);
    },

    getLevel(xp) {
        const lvl = Math.floor(xp / 100) + 1;
        return { level: lvl, currentXP: xp % 100, nextXP: 100 };
    },

    /* ============================================
       DASHBOARD
    ============================================ */
    renderDashboard() {
        const completedTasks = this.tasks.filter(t => t.status === 2).length;
        const totalMinutes = this.sessions.reduce((s, x) => s + (x.durationMinutes || 0), 0);
        const streak = this.calculateStreak();
        const xp = this.getXP();
        const { level, currentXP, nextXP } = this.getLevel(xp);
        const weekTasks = this.tasks.filter(t => t.status === 2 && t.createdAt > Date.now() - 604800000).length;
        const weekMin = this.sessions.filter(s => (s.studyDate || 0) > Date.now() - 604800000).reduce((a, x) => a + (x.durationMinutes || 0), 0);

        const upcomingTasks = this.tasks
            .filter(t => t.status !== 2 && t.dueDate > Date.now())
            .sort((a, b) => a.dueDate - b.dueDate)
            .slice(0, 5);

        const overdueTasks = this.tasks.filter(t => t.status !== 2 && t.dueDate < Date.now());

        return `
            <!-- Greeting -->
            <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px;margin-bottom:28px">
                <div>
                    <div style="font-size:28px;font-weight:800;letter-spacing:-0.5px">${this.getGreeting()}, ${this.getUserName()} 👋</div>
                    <div style="font-size:14px;color:var(--text-muted);margin-top:4px;font-style:italic">"${this.getMotivationalQuote()}"</div>
                </div>
            </div>

            <!-- Stats Cards -->
            <div class="stats-grid">
                <div class="stat-card purple">
                    <div class="stat-header">
                        <div class="stat-icon"><i class="fas fa-clock"></i></div>
                        <div class="stat-trend up"><i class="fas fa-arrow-up"></i> ${this.formatMinutes(weekMin)} this week</div>
                    </div>
                    <div class="stat-value">${this.formatMinutes(totalMinutes)}</div>
                    <div class="stat-label">Total Study Hours</div>
                    <div class="stat-progress"><div class="stat-progress-fill" style="width:${Math.min(100, (totalMinutes / 1200) * 100)}%"></div></div>
                </div>
                <div class="stat-card green">
                    <div class="stat-header">
                        <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
                        <div class="stat-trend up"><i class="fas fa-arrow-up"></i> ${weekTasks} this week</div>
                    </div>
                    <div class="stat-value">${completedTasks}</div>
                    <div class="stat-label">Tasks Completed</div>
                    <div class="stat-progress"><div class="stat-progress-fill" style="width:${this.tasks.length > 0 ? (completedTasks / this.tasks.length) * 100 : 0}%"></div></div>
                </div>
                <div class="stat-card amber">
                    <div class="stat-header">
                        <div class="stat-icon"><i class="fas fa-fire"></i></div>
                    </div>
                    <div class="stat-value">${streak}</div>
                    <div class="stat-label">Day Study Streak</div>
                    <div class="stat-progress"><div class="stat-progress-fill" style="width:${Math.min(100, streak * 10)}%"></div></div>
                </div>
                <div class="stat-card rose">
                    <div class="stat-header">
                        <div class="stat-icon"><i class="fas fa-book"></i></div>
                    </div>
                    <div class="stat-value">${this.subjects.length}</div>
                    <div class="stat-label">Subjects Enrolled</div>
                    <div class="stat-progress"><div class="stat-progress-fill" style="width:${Math.min(100, this.subjects.length * 10)}%"></div></div>
                </div>
            </div>

            <!-- AI Assistant Card -->
            <div class="ai-card">
                <div class="ai-card-content">
                    <div style="display:flex;align-items:center;gap:12px;margin-bottom:8px">
                        <i class="fas fa-robot" style="font-size:28px"></i>
                        <h2>AI Study Coach</h2>
                    </div>
                    <p>Let me analyze your workload and create the perfect study plan. I'll suggest priorities and predict completion dates based on your habits.</p>
                    <div class="ai-actions">
                        <button class="ai-btn" onclick="navigateTo('schedule')"><i class="fas fa-calendar-alt"></i> Generate Schedule</button>
                        <button class="ai-btn" onclick="showAIModal()"><i class="fas fa-lightbulb"></i> Get Insights</button>
                        <button class="ai-btn" onclick="showToast('AI analysis coming soon!','info')"><i class="fas fa-chart-line"></i> Predict Deadlines</button>
                    </div>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="quick-actions">
                <div class="action-card" onclick="navigateTo('schedule')">
                    <div class="icon" style="background:var(--primary-50);color:var(--primary)"><i class="fas fa-calendar-alt"></i></div>
                    <span>AI Schedule</span>
                </div>
                <div class="action-card" onclick="navigateTo('timer')">
                    <div class="icon" style="background:rgba(236,72,153,0.1);color:var(--secondary)"><i class="fas fa-clock"></i></div>
                    <span>Focus Timer</span>
                </div>
                <div class="action-card" onclick="showSubjectModal()">
                    <div class="icon" style="background:var(--success-50);color:var(--success)"><i class="fas fa-plus-circle"></i></div>
                    <span>Add Subject</span>
                </div>
                <div class="action-card" onclick="showTaskModal()">
                    <div class="icon" style="background:var(--warning-50);color:var(--warning)"><i class="fas fa-plus-square"></i></div>
                    <span>Add Task</span>
                </div>
            </div>

            <!-- XP Bar -->
            <div class="xp-bar-container">
                <div class="xp-header">
                    <div class="xp-level">
                        <div class="level-badge">${level}</div>
                        <div class="level-info">
                            <div class="level-name">Level ${level} Scholar</div>
                            <div class="level-xp">${xp} XP earned</div>
                        </div>
                    </div>
                    <div style="font-size:13px;color:var(--text-muted)">${currentXP}/${nextXP} XP to next level</div>
                </div>
                <div class="xp-bar"><div class="xp-bar-fill" style="width:${currentXP}%"></div></div>
            </div>

            <!-- Two columns: Upcoming + Alerts -->
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:24px">
                <div>
                    <div class="section-header"><h2>Upcoming Deadlines</h2></div>
                    <div class="item-list">
                        ${upcomingTasks.length > 0 ? upcomingTasks.map(t => this.renderTaskItem(t)).join('') :
                            '<div class="empty-state" style="padding:30px"><i class="fas fa-check-circle"></i><p>No upcoming deadlines</p></div>'}
                    </div>
                </div>
                <div>
                    <div class="section-header"><h2>Overdue</h2></div>
                    <div class="item-list">
                        ${overdueTasks.length > 0 ? overdueTasks.slice(0, 5).map(t => this.renderTaskItem(t)).join('') :
                            '<div class="empty-state" style="padding:30px"><i class="fas fa-trophy" style="color:var(--success)"></i><p style="color:var(--success)">All caught up!</p></div>'}
                    </div>
                </div>
            </div>
        `;
    },

    /* ============================================
       SUBJECTS
    ============================================ */
    renderSubjects() {
        return `
            <div class="section-header">
                <div><h2>My Subjects</h2><div class="subtitle">${this.subjects.length} subjects enrolled</div></div>
                <button class="btn-primary" style="width:auto;padding:10px 22px" onclick="showSubjectModal()"><i class="fas fa-plus"></i> Add Subject</button>
            </div>
            <div class="item-list">
                ${this.subjects.length > 0 ? this.subjects.map(s => `
                    <div class="list-item" onclick="showSubjectModal('${s.subjectId}')">
                        <div class="priority-dot" style="background:${s.color || '#6366F1'}"></div>
                        <div class="item-content">
                            <div class="item-title">${s.subjectName}</div>
                            <div class="item-subtitle">${this.getDifficultyText(s.difficultyLevel)} · Target: ${s.targetGrade || 'N/A'}</div>
                        </div>
                        <div style="text-align:right">
                            <div style="font-size:12px;color:var(--text-muted)">${this.tasks.filter(t => t.subjectId === s.subjectId && t.status !== 2).length} tasks</div>
                        </div>
                        <div class="item-actions">
                            <button onclick="event.stopPropagation();deleteSubject('${s.subjectId}')" class="delete"><i class="fas fa-trash"></i></button>
                        </div>
                    </div>
                `).join('') :
                    '<div class="empty-state"><i class="fas fa-book-open"></i><p>No subjects yet</p><button class="btn-primary" style="width:auto" onclick="showSubjectModal()">Add First Subject</button></div>'}
            </div>
        `;
    },

    /* ============================================
       TASKS (Kanban)
    ============================================ */
    renderTasks(filter = 'kanban') {
        if (filter === 'kanban') return this.renderKanban();
        let filtered = [...this.tasks];
        if (filter === 'pending') filtered = filtered.filter(t => t.status === 0);
        else if (filter === 'progress') filtered = filtered.filter(t => t.status === 1);
        else if (filter === 'completed') filtered = filtered.filter(t => t.status === 2);
        filtered.sort((a, b) => a.dueDate - b.dueDate);

        return `
            <div class="section-header">
                <div><h2>My Tasks</h2><div class="subtitle">${this.tasks.length} total tasks</div></div>
                <button class="btn-primary" style="width:auto;padding:10px 22px" onclick="showTaskModal()"><i class="fas fa-plus"></i> Add Task</button>
            </div>
            <div class="filter-bar">
                <button class="filter-chip" onclick="renderPage('tasks','kanban')"><i class="fas fa-columns"></i> Board</button>
                <button class="filter-chip ${filter === 'all' ? 'active' : ''}" onclick="renderPage('tasks','all')">All</button>
                <button class="filter-chip ${filter === 'pending' ? 'active' : ''}" onclick="renderPage('tasks','pending')">To Do</button>
                <button class="filter-chip ${filter === 'progress' ? 'active' : ''}" onclick="renderPage('tasks','progress')">In Progress</button>
                <button class="filter-chip ${filter === 'completed' ? 'active' : ''}" onclick="renderPage('tasks','completed')">Done</button>
            </div>
            <div class="item-list">
                ${filtered.length > 0 ? filtered.map(t => this.renderTaskItem(t)).join('') :
                    '<div class="empty-state"><i class="fas fa-clipboard"></i><p>No tasks found</p></div>'}
            </div>
        `;
    },

    renderKanban() {
        const todo = this.tasks.filter(t => t.status === 0).sort((a, b) => a.dueDate - b.dueDate);
        const inProgress = this.tasks.filter(t => t.status === 1).sort((a, b) => a.dueDate - b.dueDate);
        const done = this.tasks.filter(t => t.status === 2).sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));

        const renderCard = (t) => {
            const priority = t.priority === 1 ? 'high' : t.priority === 3 ? 'low' : 'medium';
            const typeColors = { assignment: '#6366F1', quiz: '#EC4899', project: '#F59E0B', exam: '#F43F5E' };
            const subject = this.subjects.find(s => s.subjectId === t.subjectId);
            return `
                <div class="kanban-card" draggable="true" data-task-id="${t.taskId}">
                    <div class="task-name">${t.taskName}</div>
                    <div class="task-meta">
                        <span class="task-tag" style="background:${typeColors[t.taskType] || '#6366F1'}20;color:${typeColors[t.taskType] || '#6366F1'}">${t.taskType || 'task'}</span>
                        <span class="task-tag badge-${priority}">${t.priority === 1 ? 'High' : t.priority === 3 ? 'Low' : 'Med'}</span>
                        ${subject ? `<span class="task-tag" style="background:${subject.color || '#6366F1'}20;color:${subject.color || '#6366F1'}">${subject.subjectName}</span>` : ''}
                        <span class="task-due">${this.formatDueDate(t.dueDate)}</span>
                    </div>
                    <div style="display:flex;gap:4px;margin-top:10px">
                        ${t.status === 0 ? `<button class="btn-ghost" style="font-size:11px;padding:4px 10px" onclick="event.stopPropagation();updateTaskStatus('${t.taskId}',1)"><i class="fas fa-play"></i> Start</button>` : ''}
                        ${t.status === 1 ? `<button class="btn-ghost" style="font-size:11px;padding:4px 10px;color:var(--success)" onclick="event.stopPropagation();updateTaskStatus('${t.taskId}',2)"><i class="fas fa-check"></i> Done</button>` : ''}
                        <button class="btn-ghost" style="font-size:11px;padding:4px 10px;color:var(--danger)" onclick="event.stopPropagation();deleteTask('${t.taskId}')"><i class="fas fa-trash"></i></button>
                    </div>
                </div>
            `;
        };

        return `
            <div class="section-header">
                <div><h2>Task Board</h2><div class="subtitle">Drag and manage your tasks</div></div>
                <div style="display:flex;gap:8px">
                    <button class="btn-ghost" onclick="renderPage('tasks','all')"><i class="fas fa-list"></i> List View</button>
                    <button class="btn-primary" style="width:auto;padding:10px 22px" onclick="showTaskModal()"><i class="fas fa-plus"></i> Add Task</button>
                </div>
            </div>
            <div class="kanban-board">
                <div class="kanban-column" id="col-todo">
                    <div class="kanban-header">
                        <div class="dot" style="background:var(--text-muted)"></div>
                        <h3>To Do</h3>
                        <span class="count">${todo.length}</span>
                    </div>
                    ${todo.length > 0 ? todo.map(renderCard).join('') : '<div style="text-align:center;padding:20px;color:var(--text-muted);font-size:13px">No tasks</div>'}
                </div>
                <div class="kanban-column" id="col-progress">
                    <div class="kanban-header">
                        <div class="dot" style="background:var(--primary)"></div>
                        <h3>In Progress</h3>
                        <span class="count">${inProgress.length}</span>
                    </div>
                    ${inProgress.length > 0 ? inProgress.map(renderCard).join('') : '<div style="text-align:center;padding:20px;color:var(--text-muted);font-size:13px">No tasks</div>'}
                </div>
                <div class="kanban-column" id="col-done">
                    <div class="kanban-header">
                        <div class="dot" style="background:var(--success)"></div>
                        <h3>Completed</h3>
                        <span class="count">${done.length}</span>
                    </div>
                    ${done.length > 0 ? done.slice(0, 10).map(renderCard).join('') : '<div style="text-align:center;padding:20px;color:var(--text-muted);font-size:13px">No tasks</div>'}
                </div>
            </div>
        `;
    },

    renderTaskItem(task) {
        const priority = task.priority === 1 ? 'high' : task.priority === 3 ? 'low' : 'medium';
        const statusClass = task.status === 2 ? 'completed' : task.status === 1 ? 'progress' : 'pending';
        const isOverdue = task.dueDate < Date.now() && task.status !== 2;
        const subject = this.subjects.find(s => s.subjectId === task.subjectId);
        const typeColors = { assignment: '#6366F1', quiz: '#EC4899', project: '#F59E0B', exam: '#F43F5E' };

        return `
            <div class="list-item">
                <div class="priority-bar ${priority}"></div>
                <input type="checkbox" ${task.status === 2 ? 'checked' : ''}
                    onchange="toggleTaskStatus('${task.taskId}', this.checked)"
                    style="width:20px;height:20px;accent-color:var(--primary);cursor:pointer;flex-shrink:0">
                <div class="item-content">
                    <div class="item-title" style="${task.status === 2 ? 'text-decoration:line-through;opacity:0.5' : ''}">${task.taskName}</div>
                    <div class="item-subtitle">
                        <span style="color:${typeColors[task.taskType] || '#6366F1'};font-weight:600">${task.taskType || 'task'}</span>
                        ${subject ? ` · ${subject.subjectName}` : ''}
                    </div>
                </div>
                <span class="item-badge badge-${statusClass}">${this.getStatusText(task.status)}</span>
                <span style="font-size:12px;color:${isOverdue ? 'var(--danger)' : 'var(--text-muted)'};white-space:nowrap;font-weight:500">${this.formatDueDate(task.dueDate)}</span>
                <div class="item-actions">
                    <button onclick="event.stopPropagation();showTaskModal('${task.taskId}')"><i class="fas fa-edit"></i></button>
                    <button onclick="event.stopPropagation();deleteTask('${task.taskId}')" class="delete"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `;
    },

    /* ============================================
       SCHEDULE
    ============================================ */
    renderSchedule() {
        const settings = JSON.parse(localStorage.getItem('studyflow_settings') || '{}');
        const hours = settings.availableHours || 4;
        const result = ScheduleGenerator.generate(this.subjects, this.tasks, hours);
        const overload = ScheduleGenerator.detectOverload(this.subjects, this.tasks, hours);

        return `
            <div class="section-header">
                <div><h2>AI Study Schedule</h2><div class="subtitle">Optimized weekly study plan</div></div>
            </div>

            <div class="ai-card" style="margin-bottom:24px">
                <div class="ai-card-content">
                    <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
                        <i class="fas fa-robot" style="font-size:24px"></i>
                        <h2 style="font-size:20px">Generate AI Schedule</h2>
                    </div>
                    <p style="margin-bottom:16px">Let AI plan your optimal study time based on your subjects, difficulty levels, and deadlines.</p>
                    <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
                        <label style="font-size:13px;font-weight:600">Available hours/day:</label>
                        <input type="range" min="1" max="12" value="${hours}" id="hours-slider"
                            onchange="updateHours(this.value)" style="width:140px;accent-color:white">
                        <span id="hours-display" style="font-weight:800;font-size:18px">${hours}h</span>
                    </div>
                </div>
            </div>

            ${overload ? `<div style="background:var(--warning-50);color:var(--warning);padding:14px 18px;border-radius:var(--radius);margin-bottom:20px;font-size:13px;font-weight:500;border:1px solid rgba(245,158,11,0.15)"><i class="fas fa-exclamation-triangle"></i> ${overload}</div>` : ''}

            <div style="display:grid;grid-template-columns:1fr 2fr;gap:20px">
                <div>
                    <div class="section-header"><h2>Subject Priority</h2></div>
                    ${result.priorities.length > 0 ? result.priorities.map(p => `
                        <div class="priority-item">
                            <div class="priority-rank">#${p.rank}</div>
                            <div class="priority-dot" style="background:${p.color}"></div>
                            <div class="priority-info">
                                <div class="priority-name">${p.subjectName}</div>
                                <div class="priority-reason">${p.reason}</div>
                            </div>
                        </div>
                    `).join('') : '<div class="empty-state" style="padding:30px"><p>Add subjects to generate</p></div>'}
                </div>

                <div>
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
                                `).join('') : '<div style="font-size:12px;color:var(--text-muted);padding:6px 0">Rest day</div>'}
                            </div>
                        `).join('')}
                    </div>
                </div>
            </div>
        `;
    },

    /* ============================================
       TIMER
    ============================================ */
    renderTimer() {
        PomodoroTimer.init();
        return `
            <div class="section-header"><div><h2>Focus Zone</h2><div class="subtitle">Deep work with Pomodoro technique</div></div></div>
            <div class="timer-container">
                <div class="timer-ring" id="timer-ring">
                    <div class="timer-display">
                        <div class="timer-time" id="timer-time">25:00</div>
                        <div class="timer-status" id="timer-status">Focus Mode</div>
                    </div>
                </div>
                <div class="timer-controls">
                    <button class="btn-timer-secondary" onclick="PomodoroTimer.reset()" title="Reset"><i class="fas fa-redo"></i></button>
                    <button class="btn-timer-main" id="btn-play" onclick="PomodoroTimer.start()" title="Start"><i class="fas fa-play"></i></button>
                    <button class="btn-timer-main" id="btn-pause" onclick="PomodoroTimer.pause()" title="Pause" style="display:none"><i class="fas fa-pause"></i></button>
                    <button class="btn-timer-secondary" onclick="PomodoroTimer.skip()" title="Skip"><i class="fas fa-forward"></i></button>
                </div>
                <div class="timer-sessions">
                    <div class="count" id="timer-sessions-count">0</div>
                    <div class="label">Sessions Completed</div>
                </div>
            </div>
        `;
    },

    /* ============================================
       ANALYTICS
    ============================================ */
    renderAnalytics() {
        const totalMin = this.sessions.reduce((s, x) => s + (x.durationMinutes || 0), 0);
        const done = this.tasks.filter(t => t.status === 2).length;
        const total = this.tasks.length;
        const rate = total > 0 ? Math.round((done / total) * 100) : 0;
        const streak = this.calculateStreak();
        const xp = this.getXP();

        const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        const dailyMin = new Array(7).fill(0);
        this.sessions.forEach(s => {
            const d = new Date(s.studyDate || Date.now()).getDay();
            dailyMin[d === 0 ? 6 : d - 1] += s.durationMinutes || 0;
        });
        const maxD = Math.max(...dailyMin, 1);

        const typeCounts = { assignment: 0, quiz: 0, project: 0, exam: 0 };
        this.tasks.forEach(t => { if (typeCounts.hasOwnProperty(t.taskType)) typeCounts[t.taskType]++; });

        return `
            <div class="section-header"><div><h2>Analytics</h2><div class="subtitle">Your productivity insights</div></div></div>

            <div class="stats-grid" style="grid-template-columns:repeat(3,1fr)">
                <div class="stat-card purple">
                    <div class="stat-header"><div class="stat-icon"><i class="fas fa-clock"></i></div></div>
                    <div class="stat-value">${this.formatMinutes(totalMin)}</div>
                    <div class="stat-label">Total Focus Time</div>
                </div>
                <div class="stat-card green">
                    <div class="stat-header"><div class="stat-icon"><i class="fas fa-bullseye"></i></div></div>
                    <div class="stat-value">${rate}%</div>
                    <div class="stat-label">Completion Rate</div>
                </div>
                <div class="stat-card amber">
                    <div class="stat-header"><div class="stat-icon"><i class="fas fa-trophy"></i></div></div>
                    <div class="stat-value">${xp}</div>
                    <div class="stat-label">Total XP Earned</div>
                </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:24px">
                <div class="chart-container">
                    <h3>Weekly Study Hours</h3>
                    <div class="bar-chart" style="margin-bottom:32px">
                        ${days.map((day, i) => `
                            <div class="bar" style="height:${(dailyMin[i] / maxD) * 100}%;background:linear-gradient(180deg,var(--primary),var(--accent))">
                                <div class="bar-value">${dailyMin[i]}m</div>
                                <div class="bar-label">${day}</div>
                            </div>
                        `).join('')}
                    </div>
                </div>

                <div class="chart-container">
                    <h3>Task Distribution</h3>
                    <div class="bar-chart" style="margin-bottom:32px;height:140px">
                        ${Object.entries(typeCounts).map(([type, count]) => {
                            const colors = { assignment: '#6366F1', quiz: '#EC4899', project: '#F59E0B', exam: '#F43F5E' };
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
            </div>

            <!-- Achievements -->
            <div class="section-header"><h2>Achievements</h2></div>
            <div class="achievements-grid">
                ${this.renderAchievement('🎯', 'First Task', 'Complete your first task', done >= 1)}
                ${this.renderAchievement('🔥', 'On Fire', '3-day study streak', streak >= 3)}
                ${this.renderAchievement('📚', 'Bookworm', 'Enroll in 3 subjects', this.subjects.length >= 3)}
                ${this.renderAchievement('⏰', 'Focused', 'Complete 5 sessions', this.sessions.length >= 5)}
                ${this.renderAchievement('🏆', 'Champion', 'Complete 10 tasks', done >= 10)}
                ${this.renderAchievement('💎', 'Diamond', 'Earn 500 XP', xp >= 500)}
                ${this.renderAchievement('🌟', 'Rising Star', 'Reach Level 3', Math.floor(xp / 100) + 1 >= 3)}
                ${this.renderAchievement('🧠', 'Genius', 'Complete 20 tasks', done >= 20)}
            </div>
        `;
    },

    renderAchievement(icon, name, desc, unlocked) {
        return `
            <div class="achievement-card ${unlocked ? '' : 'locked'}">
                <div class="badge-icon">${icon}</div>
                <div class="badge-name">${name}</div>
                <div class="badge-desc">${desc}</div>
            </div>
        `;
    }
};
