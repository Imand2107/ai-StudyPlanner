// Main App
let currentPage = 'dashboard';
let currentTaskFilter = 'all';

// Navigation
function navigateTo(page, ...args) {
    currentPage = page;
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.page === page);
    });

    document.getElementById('page-title').textContent = {
        dashboard: 'Dashboard',
        subjects: 'Subjects',
        tasks: 'Tasks',
        schedule: 'AI Schedule',
        timer: 'Focus Timer',
        analytics: 'Analytics'
    }[page] || 'Dashboard';

    renderPage(page, ...args);
    closeSidebar();
}

async function renderPage(page, ...args) {
    await Pages.loadData();
    const container = document.getElementById('page-container');

    switch (page) {
        case 'dashboard': container.innerHTML = Pages.renderDashboard(); break;
        case 'subjects': container.innerHTML = Pages.renderSubjects(); break;
        case 'tasks': container.innerHTML = Pages.renderTasks(args[0] || currentTaskFilter); break;
        case 'schedule': container.innerHTML = Pages.renderSchedule(); break;
        case 'timer': container.innerHTML = Pages.renderTimer(); break;
        case 'analytics': container.innerHTML = Pages.renderAnalytics(); break;
    }
}

// Sidebar
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

function closeSidebar() {
    document.getElementById('sidebar').classList.remove('open');
}

// Dark Mode
function toggleDarkMode() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    document.documentElement.setAttribute('data-theme', isDark ? '' : 'dark');
    localStorage.setItem('studyflow_theme', isDark ? 'light' : 'dark');
    const icon = document.getElementById('theme-icon');
    icon.className = isDark ? 'fas fa-moon' : 'fas fa-sun';
}

// Toast
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i> ${message}`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Modal
function showModal(title, content) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = content;
    document.getElementById('modal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('modal').classList.add('hidden');
}

// Subject Modal
function showSubjectModal(subjectId) {
    const subject = subjectId ? Pages.subjects.find(s => s.subjectId === subjectId) : null;
    const isEdit = !!subject;
    const colors = ['#6C63FF', '#FF6584', '#FFA502', '#2ED573', '#3B82F6', '#00BCD4', '#9C27B0', '#E91E63'];

    showModal(isEdit ? 'Edit Subject' : 'Add Subject', `
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
            <div class="flex gap-8" style="flex-wrap:wrap">
                ${colors.map(c => `
                    <div onclick="document.getElementById('modal-subject-color').value='${c}';document.querySelectorAll('.color-opt').forEach(e=>e.style.outline='none');this.style.outline='3px solid var(--primary)'"
                        class="color-opt"
                        style="width:32px;height:32px;border-radius:8px;background:${c};cursor:pointer;${subject?.color === c ? 'outline:3px solid var(--primary)' : ''}"></div>
                `).join('')}
            </div>
            <input type="hidden" id="modal-subject-color" value="${subject?.color || colors[0]}">
        </div>
        <button class="btn-primary" onclick="saveSubject(${isEdit ? `'${subjectId}'` : 'null'})">
            <i class="fas fa-save"></i> ${isEdit ? 'Update' : 'Save'}
        </button>
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
        if (subjectId) {
            await Database.updateSubject(subjectId, data);
            showToast('Subject updated!', 'success');
        } else {
            await Database.addSubject(data);
            showToast('Subject added!', 'success');
        }
        closeModal();
        renderPage(currentPage);
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

async function deleteSubject(subjectId) {
    if (!confirm('Delete this subject?')) return;
    try {
        await Database.deleteSubject(subjectId);
        showToast('Subject deleted', 'success');
        renderPage(currentPage);
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

// Task Modal
function showTaskModal(taskId) {
    const task = taskId ? Pages.tasks.find(t => t.taskId === taskId) : null;
    const isEdit = !!task;

    showModal(isEdit ? 'Edit Task' : 'Add Task', `
        <div class="form-group">
            <label>Task Name</label>
            <input type="text" id="modal-task-name" value="${task?.taskName || ''}" placeholder="e.g., Math Assignment 3">
        </div>
        <div class="form-group">
            <label>Subject</label>
            <select id="modal-task-subject">
                <option value="">Select Subject</option>
                ${Pages.subjects.map(s => `
                    <option value="${s.subjectId}" ${task?.subjectId === s.subjectId ? 'selected' : ''}>${s.subjectName}</option>
                `).join('')}
            </select>
        </div>
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
        <div class="form-group">
            <label>Due Date</label>
            <input type="datetime-local" id="modal-task-due" value="${task?.dueDate ? new Date(task.dueDate).toISOString().slice(0, 16) : ''}">
        </div>
        <button class="btn-primary" onclick="saveTask(${isEdit ? `'${taskId}'` : 'null'})">
            <i class="fas fa-save"></i> ${isEdit ? 'Update' : 'Save'}
        </button>
    `);
}

async function saveTask(taskId) {
    const name = document.getElementById('modal-task-name').value.trim();
    const subjectId = document.getElementById('modal-task-subject').value;
    const dueDate = new Date(document.getElementById('modal-task-due').value).getTime();

    if (!name) { showToast('Please enter a task name', 'error'); return; }
    if (!subjectId) { showToast('Please select a subject', 'error'); return; }
    if (!dueDate) { showToast('Please select a due date', 'error'); return; }

    const data = {
        taskName: name,
        subjectId,
        taskType: document.getElementById('modal-task-type').value,
        priority: parseInt(document.getElementById('modal-task-priority').value),
        dueDate,
        status: taskId ? Pages.tasks.find(t => t.taskId === taskId)?.status || 0 : 0,
        userId: currentUser.uid
    };

    try {
        if (taskId) {
            await Database.updateTask(taskId, data);
            showToast('Task updated!', 'success');
        } else {
            await Database.addTask(data);
            showToast('Task added!', 'success');
        }
        closeModal();
        renderPage(currentPage);
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

async function toggleTaskStatus(taskId, checked) {
    try {
        await Database.updateTask(taskId, { status: checked ? 2 : 0 });
        renderPage(currentPage);
    } catch (e) {
        showToast('Error updating task', 'error');
    }
}

async function deleteTask(taskId) {
    if (!confirm('Delete this task?')) return;
    try {
        await Database.deleteTask(taskId);
        showToast('Task deleted', 'success');
        renderPage(currentPage);
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

// Schedule
function updateHours(value) {
    document.getElementById('hours-display').textContent = value + 'h';
    const settings = JSON.parse(localStorage.getItem('studyflow_settings') || '{}');
    settings.availableHours = parseInt(value);
    localStorage.setItem('studyflow_settings', JSON.stringify(settings));
    renderPage('schedule');
}

// Notifications (placeholder)
function showNotifications() {
    showToast('Notifications coming soon!', 'info');
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    // Load theme
    const savedTheme = localStorage.getItem('studyflow_theme');
    if (savedTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        document.getElementById('theme-icon').className = 'fas fa-sun';
    }

    // Check stored auth
    const storedUser = localStorage.getItem('studyflow_user');
    if (storedUser) {
        try {
            const user = JSON.parse(storedUser);
            if (user.uid) {
                // Firebase will handle auth state
            }
        } catch (e) {}
    }
});
