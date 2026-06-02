let currentPage = 'dashboard';

/* NAVIGATION */
function navigateTo(page, ...args) {
    currentPage = page;
    document.querySelectorAll('.nav-item').forEach(i => i.classList.toggle('active', i.dataset.page === page));
    renderPage(page, ...args);
    closeSidebar();
}

async function renderPage(page, ...args) {
    await Pages.loadData();
    const c = document.getElementById('page-container');
    c.style.animation = 'none'; c.offsetHeight; c.style.animation = '';
    switch (page) {
        case 'dashboard': c.innerHTML = Pages.renderDashboard(); animateCounters(); break;
        case 'subjects': c.innerHTML = Pages.renderSubjects(); break;
        case 'tasks': c.innerHTML = Pages.renderTasks(args[0] || 'kanban'); break;
        case 'schedule': c.innerHTML = Pages.renderSchedule(); break;
        case 'timer': c.innerHTML = Pages.renderTimer(); break;
        case 'analytics': c.innerHTML = Pages.renderAnalytics(); animateCounters(); break;
    }
}

/* SIDEBAR */
function toggleSidebar() { document.getElementById('sidebar').classList.toggle('open'); }
function closeSidebar() { document.getElementById('sidebar').classList.remove('open'); }

/* DARK MODE */
function toggleDarkMode() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    document.documentElement.setAttribute('data-theme', isDark ? '' : 'dark');
    localStorage.setItem('studyflow_theme', isDark ? 'light' : 'dark');
    const icon = document.getElementById('theme-icon');
    if (icon) icon.className = isDark ? 'fas fa-moon' : 'fas fa-sun';
}

/* TOAST */
function showToast(msg, type = 'info') {
    const c = document.getElementById('toast-container');
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    const icons = { success: 'check-circle', error: 'exclamation-circle', info: 'info-circle' };
    t.innerHTML = `<i class="fas fa-${icons[type] || 'info-circle'}"></i> ${msg}`;
    c.appendChild(t);
    setTimeout(() => { t.style.opacity = '0'; t.style.transform = 'translateX(100px)'; setTimeout(() => t.remove(), 300); }, 3000);
}

/* MODAL */
function showModal(title, content) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = content;
    document.getElementById('modal').classList.remove('hidden');
}
function closeModal() { document.getElementById('modal').classList.add('hidden'); }

/* CONFETTI */
function fireConfetti() {
    const c = document.createElement('div');
    c.className = 'confetti-container';
    document.body.appendChild(c);
    const colors = ['#6366F1', '#EC4899', '#10B981', '#F59E0B', '#F43F5E', '#0EA5E9', '#8B5CF6'];
    for (let i = 0; i < 60; i++) {
        const p = document.createElement('div');
        p.className = 'confetti-piece';
        p.style.left = Math.random() * 100 + '%';
        p.style.background = colors[Math.floor(Math.random() * colors.length)];
        p.style.animationDelay = Math.random() * 0.5 + 's';
        p.style.animationDuration = (2 + Math.random() * 2) + 's';
        p.style.width = (6 + Math.random() * 8) + 'px';
        p.style.height = (6 + Math.random() * 8) + 'px';
        c.appendChild(p);
    }
    setTimeout(() => c.remove(), 4000);
}

/* ANIMATED COUNTERS */
function animateCounters() {
    document.querySelectorAll('.stat-value').forEach(el => {
        const text = el.textContent;
        const match = text.match(/([\d.]+)(.*)/);
        if (!match) return;
        const target = parseFloat(match[1]);
        const suffix = match[2] || '';
        if (isNaN(target) || target === 0) return;
        let current = 0;
        const step = target / 30;
        const timer = setInterval(() => {
            current += step;
            if (current >= target) { current = target; clearInterval(timer); }
            el.textContent = (Number.isInteger(target) ? Math.round(current) : current.toFixed(1)) + suffix;
        }, 25);
    });
}

/* USER MENU */
function toggleUserMenu() {
    document.getElementById('user-menu').classList.toggle('hidden');
}

/* GLOBAL SEARCH */
function handleSearch(value) {
    if (!value.trim()) return;
    openCommandPalette(value.trim());
}

function handleCommand(value) {
    const results = document.getElementById('command-results');
    if (!value.trim()) {
        results.innerHTML = getDefaultCommands();
        return;
    }
    const q = value.toLowerCase();
    const matchedSubjects = Pages.subjects.filter(s => s.subjectName.toLowerCase().includes(q));
    const matchedTasks = Pages.tasks.filter(t => t.taskName.toLowerCase().includes(q));
    let html = '';
    matchedSubjects.forEach(s => {
        html += `<div class="command-result" onclick="closeCommandPalette();navigateTo('subjects')"><i class="fas fa-book" style="color:${s.color || 'var(--primary)'}"></i><span>${s.subjectName}</span></div>`;
    });
    matchedTasks.forEach(t => {
        html += `<div class="command-result" onclick="closeCommandPalette();navigateTo('tasks')"><i class="fas fa-check-circle"></i><span>${t.taskName}</span></div>`;
    });
    if (!html) html = '<div style="padding:16px;text-align:center;color:var(--text-3);font-size:13px">No results found</div>';
    results.innerHTML = html;
}

function getDefaultCommands() {
    return `
        <div class="command-result" onclick="closeCommandPalette();navigateTo('dashboard')"><i class="fas fa-th-large"></i><span>Dashboard</span></div>
        <div class="command-result" onclick="closeCommandPalette();navigateTo('subjects')"><i class="fas fa-book"></i><span>Subjects</span></div>
        <div class="command-result" onclick="closeCommandPalette();navigateTo('tasks')"><i class="fas fa-check-circle"></i><span>Tasks</span></div>
        <div class="command-result" onclick="closeCommandPalette();navigateTo('schedule')"><i class="fas fa-calendar-alt"></i><span>AI Schedule</span></div>
        <div class="command-result" onclick="closeCommandPalette();navigateTo('timer')"><i class="fas fa-clock"></i><span>Focus Zone</span></div>
        <div class="command-result" onclick="closeCommandPalette();navigateTo('analytics')"><i class="fas fa-chart-bar"></i><span>Analytics</span></div>
        <div class="command-result" onclick="closeCommandPalette();showSubjectModal()"><i class="fas fa-plus-circle"></i><span>Add Subject</span></div>
        <div class="command-result" onclick="closeCommandPalette();showTaskModal()"><i class="fas fa-plus-square"></i><span>Add Task</span></div>
        <div class="command-result" onclick="closeCommandPalette();toggleDarkMode()"><i class="fas fa-moon"></i><span>Toggle Theme</span></div>
        <div class="command-result" onclick="closeCommandPalette();logout()"><i class="fas fa-sign-out-alt"></i><span>Sign Out</span></div>
    `;
}

function openCommandPalette(prefill) {
    const palette = document.getElementById('command-palette');
    palette.classList.remove('hidden');
    const input = document.getElementById('command-input');
    input.value = prefill || '';
    input.focus();
    handleCommand(input.value);
}

function closeCommandPalette() {
    document.getElementById('command-palette').classList.add('hidden');
}

/* NOTIFICATIONS */
function showNotifications() { showToast('Notifications coming soon!', 'info'); }

/* SUBJECT MODAL */
function showSubjectModal(subjectId) {
    const subject = subjectId ? Pages.subjects.find(s => s.subjectId === subjectId) : null;
    const isEdit = !!subject;
    const colors = ['#6366F1', '#EC4899', '#F59E0B', '#10B981', '#0EA5E9', '#8B5CF6', '#F43F5E', '#14B8A6'];

    showModal(isEdit ? 'Edit Subject' : 'Add New Subject', `
        <div class="form-group">
            <label>Subject Name</label>
            <input type="text" id="modal-subject-name" value="${subject?.subjectName || ''}" placeholder="e.g., Mathematics">
        </div>
        <div class="form-group">
            <label>Difficulty Level</label>
            <select id="modal-subject-difficulty">
                <option value="1" ${subject?.difficultyLevel === 1 ? 'selected' : ''}>Easy</option>
                <option value="2" ${(!subject || subject?.difficultyLevel === 2) ? 'selected' : ''}>Medium</option>
                <option value="3" ${subject?.difficultyLevel === 3 ? 'selected' : ''}>Hard</option>
                <option value="4" ${subject?.difficultyLevel === 4 ? 'selected' : ''}>Very Hard</option>
            </select>
        </div>
        <div class="form-group">
            <label>Target Grade</label>
            <input type="text" id="modal-subject-grade" value="${subject?.targetGrade || ''}" placeholder="e.g., A, B+, 90%">
        </div>
        <div class="form-group">
            <label>Color</label>
            <div style="display:flex;gap:8px;flex-wrap:wrap">
                ${colors.map(c => `
                    <div onclick="document.getElementById('modal-subject-color').value='${c}';document.querySelectorAll('.color-opt').forEach(e=>e.style.outline='none');this.style.outline='3px solid var(--primary)';this.style.outlineOffset='2px'"
                        class="color-opt"
                        style="width:36px;height:36px;border-radius:10px;background:${c};cursor:pointer;${subject?.color === c ? 'outline:3px solid var(--primary);outline-offset:2px' : ''}"></div>
                `).join('')}
            </div>
            <input type="hidden" id="modal-subject-color" value="${subject?.color || colors[0]}">
        </div>
        <button class="btn-primary" onclick="saveSubject(${isEdit ? `'${subjectId}'` : 'null'})"><i class="fas fa-save"></i> ${isEdit ? 'Update Subject' : 'Save Subject'}</button>
    `);
}

async function saveSubject(subjectId) {
    const name = document.getElementById('modal-subject-name').value.trim();
    if (!name) { showToast('Please enter a subject name', 'error'); return; }
    const data = {
        subjectName: name,
        difficultyLevel: parseInt(document.getElementById('modal-subject-difficulty').value),
        targetGrade: document.getElementById('modal-subject-grade').value.trim(),
        color: document.getElementById('modal-subject-color').value,
        userId: currentUser.uid
    };
    try {
        if (subjectId) { await Database.updateSubject(subjectId, data); showToast('Subject updated!', 'success'); }
        else { await Database.addSubject(data); showToast('Subject added!', 'success'); fireConfetti(); }
        closeModal(); renderPage(currentPage);
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
}

async function deleteSubject(id) {
    if (!confirm('Delete this subject?')) return;
    try { await Database.deleteSubject(id); showToast('Subject deleted', 'success'); renderPage(currentPage); }
    catch (e) { showToast('Error: ' + e.message, 'error'); }
}

/* TASK MODAL */
function showTaskModal(taskId) {
    const task = taskId ? Pages.tasks.find(t => t.taskId === taskId) : null;
    const isEdit = !!task;

    showModal(isEdit ? 'Edit Task' : 'Add New Task', `
        <div class="form-group">
            <label>Task Name</label>
            <input type="text" id="modal-task-name" value="${task?.taskName || ''}" placeholder="e.g., Math Assignment 3">
        </div>
        <div class="form-group">
            <label>Subject</label>
            <select id="modal-task-subject">
                <option value="">Select Subject</option>
                ${Pages.subjects.map(s => `<option value="${s.subjectId}" ${task?.subjectId === s.subjectId ? 'selected' : ''}>${s.subjectName}</option>`).join('')}
            </select>
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
            <div class="form-group">
                <label>Task Type</label>
                <select id="modal-task-type">
                    <option value="assignment" ${task?.taskType === 'assignment' ? 'selected' : ''}>Assignment</option>
                    <option value="quiz" ${task?.taskType === 'quiz' ? 'selected' : ''}>Quiz</option>
                    <option value="project" ${task?.taskType === 'project' ? 'selected' : ''}>Project</option>
                    <option value="exam" ${task?.taskType === 'exam' ? 'selected' : ''}>Exam</option>
                </select>
            </div>
            <div class="form-group">
                <label>Priority</label>
                <select id="modal-task-priority">
                    <option value="1" ${task?.priority === 1 ? 'selected' : ''}>High</option>
                    <option value="2" ${(!task || task?.priority === 2) ? 'selected' : ''}>Medium</option>
                    <option value="3" ${task?.priority === 3 ? 'selected' : ''}>Low</option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label>Due Date</label>
            <input type="datetime-local" id="modal-task-due" value="${task?.dueDate ? new Date(task.dueDate).toISOString().slice(0, 16) : ''}">
        </div>
        <button class="btn-primary" onclick="saveTask(${isEdit ? `'${taskId}'` : 'null'})"><i class="fas fa-save"></i> ${isEdit ? 'Update Task' : 'Save Task'}</button>
    `);
}

async function saveTask(taskId) {
    const name = document.getElementById('modal-task-name').value.trim();
    const subjectId = document.getElementById('modal-task-subject').value;
    const dueDate = new Date(document.getElementById('modal-task-due').value).getTime();
    if (!name) { showToast('Please enter a task name', 'error'); return; }
    if (!subjectId) { showToast('Please select a subject', 'error'); return; }
    if (!dueDate || isNaN(dueDate)) { showToast('Please select a due date', 'error'); return; }
    const data = {
        taskName: name, subjectId,
        taskType: document.getElementById('modal-task-type').value,
        priority: parseInt(document.getElementById('modal-task-priority').value),
        dueDate,
        status: taskId ? Pages.tasks.find(t => t.taskId === taskId)?.status || 0 : 0,
        userId: currentUser.uid
    };
    try {
        if (taskId) { await Database.updateTask(taskId, data); showToast('Task updated!', 'success'); }
        else { await Database.addTask(data); showToast('Task added!', 'success'); fireConfetti(); }
        closeModal(); renderPage(currentPage);
    } catch (e) { showToast('Error: ' + e.message, 'error'); }
}

async function toggleTaskStatus(taskId, checked) {
    try { await Database.updateTask(taskId, { status: checked ? 2 : 0 }); renderPage(currentPage); }
    catch (e) { showToast('Error updating task', 'error'); }
}

async function updateTaskStatus(taskId, status) {
    try { await Database.updateTask(taskId, { status }); renderPage(currentPage); }
    catch (e) { showToast('Error updating task', 'error'); }
}

async function deleteTask(taskId) {
    if (!confirm('Delete this task?')) return;
    try { await Database.deleteTask(taskId); showToast('Task deleted', 'success'); renderPage(currentPage); }
    catch (e) { showToast('Error: ' + e.message, 'error'); }
}

/* SCHEDULE HELPERS */
function updateHours(value) {
    document.getElementById('hours-display').textContent = value + 'h';
    const settings = JSON.parse(localStorage.getItem('studyflow_settings') || '{}');
    settings.availableHours = parseInt(value);
    localStorage.setItem('studyflow_settings', JSON.stringify(settings));
    renderPage('schedule');
}

/* AI MODAL */
function showAIModal() {
    const pending = Pages.tasks.filter(t => t.status !== 2).length;
    const overdue = Pages.tasks.filter(t => t.status !== 2 && t.dueDate < Date.now()).length;
    const urgent = Pages.tasks.filter(t => t.status !== 2 && t.dueDate > Date.now() && t.dueDate < Date.now() + 604800000).length;

    let insights = [];
    if (overdue > 0) insights.push(`<div style="padding:10px 14px;background:var(--danger-50);border-radius:var(--r-sm);font-size:13px;border-left:3px solid var(--danger)"><b>${overdue} overdue task(s)</b> — Catch up on these ASAP!</div>`);
    if (urgent > 0) insights.push(`<div style="padding:10px 14px;background:var(--warning-50);border-radius:var(--r-sm);font-size:13px;border-left:3px solid var(--warning)"><b>${urgent} task(s) due this week</b> — Plan your study sessions carefully.</div>`);
    if (Pages.subjects.length > 0) {
        const hardest = Pages.subjects.reduce((a, b) => (a.difficultyLevel || 2) > (b.difficultyLevel || 2) ? a : b);
        insights.push(`<div style="padding:10px 14px;background:var(--primary-50);border-radius:var(--r-sm);font-size:13px;border-left:3px solid var(--primary)"><b>${hardest.subjectName}</b> is your most challenging subject — dedicate more study time.</div>`);
    }
    if (insights.length === 0) insights.push(`<div style="padding:10px 14px;background:var(--success-50);border-radius:var(--r-sm);font-size:13px;border-left:3px solid var(--success)">You're doing great! Keep up the momentum.</div>`);

    showModal('AI Study Insights', `
        <div style="display:flex;flex-direction:column;gap:10px;margin-bottom:20px">
            ${insights.join('')}
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
            <button class="btn-primary" style="width:auto" onclick="closeModal();navigateTo('schedule')"><i class="fas fa-calendar-alt"></i> Generate Schedule</button>
            <button class="btn-secondary" onclick="closeModal()">Close</button>
        </div>
    `);
}

/* INIT */
document.addEventListener('DOMContentLoaded', () => {
    // Theme
    const savedTheme = localStorage.getItem('studyflow_theme');
    if (savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        const icon = document.getElementById('theme-icon');
        if (icon) icon.className = 'fas fa-sun';
    }

    // Command palette keyboard shortcut
    document.addEventListener('keydown', (e) => {
        if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
            e.preventDefault();
            openCommandPalette();
        }
        if (e.key === 'Escape') {
            closeCommandPalette();
            closeModal();
            document.getElementById('user-menu')?.classList.add('hidden');
        }
    });

    // Sidebar nav click delegation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => navigateTo(item.dataset.page));
    });

    // Close user menu on outside click
    document.addEventListener('click', (e) => {
        const menu = document.getElementById('user-menu');
        const trigger = document.querySelector('.topbar-user');
        if (menu && !menu.contains(e.target) && !trigger?.contains(e.target)) {
            menu.classList.add('hidden');
        }
    });
});
