// Pomodoro Timer
const PomodoroTimer = {
    duration: 25 * 60,
    timeLeft: 25 * 60,
    isRunning: false,
    isBreak: false,
    sessionsCompleted: 0,
    timer: null,
    startTimestamp: null,

    init() {
        this.loadSettings();
        this.updateDisplay();
    },

    loadSettings() {
        const settings = JSON.parse(localStorage.getItem('studyflow_timer') || '{}');
        this.duration = (settings.pomodoro || 25) * 60;
        this.timeLeft = this.duration;
    },

    start() {
        if (this.isRunning) return;
        this.isRunning = true;
        this.startTimestamp = Date.now();
        this.timer = setInterval(() => this.tick(), 1000);
        this.updateControls();
    },

    pause() {
        this.isRunning = false;
        clearInterval(this.timer);
        this.updateControls();
    },

    reset() {
        this.pause();
        this.isBreak = false;
        this.timeLeft = this.duration;
        this.updateDisplay();
        this.updateControls();
    },

    skip() {
        clearInterval(this.timer);
        this.isRunning = false;

        if (!this.isBreak) {
            this.sessionsCompleted++;
            this.saveSession();
            this.isBreak = true;
            const settings = JSON.parse(localStorage.getItem('studyflow_timer') || '{}');
            this.timeLeft = (this.sessionsCompleted % 4 === 0 ?
                (settings.longBreak || 15) : (settings.shortBreak || 5)) * 60;
        } else {
            this.isBreak = false;
            this.timeLeft = this.duration;
        }

        this.updateDisplay();
        this.updateControls();
    },

    tick() {
        this.timeLeft--;
        this.updateDisplay();

        if (this.timeLeft <= 0) {
            this.isRunning = false;
            clearInterval(this.timer);
            this.playNotification();

            if (!this.isBreak) {
                this.sessionsCompleted++;
                this.saveSession();
                this.isBreak = true;
                const settings = JSON.parse(localStorage.getItem('studyflow_timer') || '{}');
                this.timeLeft = (this.sessionsCompleted % 4 === 0 ?
                    (settings.longBreak || 15) : (settings.shortBreak || 5)) * 60;
                showToast('Session complete! Take a break.', 'success');
            } else {
                this.isBreak = false;
                this.timeLeft = this.duration;
                showToast('Break over! Time to focus.', 'info');
            }

            this.updateDisplay();
            this.updateControls();
        }
    },

    updateDisplay() {
        const minutes = Math.floor(this.timeLeft / 60);
        const seconds = this.timeLeft % 60;
        const timeEl = document.getElementById('timer-time');
        const statusEl = document.getElementById('timer-status');
        const sessionsEl = document.getElementById('timer-sessions-count');

        if (timeEl) timeEl.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        if (statusEl) statusEl.textContent = this.isBreak ? 'Break Time' : 'Focus Mode';
        if (sessionsEl) sessionsEl.textContent = this.sessionsCompleted;

        // Update progress ring
        const progress = ((this.duration - this.timeLeft) / this.duration) * 100;
        const ring = document.querySelector('.timer-ring');
        if (ring) {
            ring.style.background = `conic-gradient(${this.isBreak ? '#FF6584' : '#6C63FF'} ${progress}%, ${getComputedStyle(document.documentElement).getPropertyValue('--surface-variant')} ${progress}%)`;
        }
    },

    updateControls() {
        const playBtn = document.getElementById('btn-play');
        const pauseBtn = document.getElementById('btn-pause');

        if (playBtn) playBtn.style.display = this.isRunning ? 'none' : 'flex';
        if (pauseBtn) pauseBtn.style.display = this.isRunning ? 'flex' : 'none';
    },

    async saveSession() {
        if (!currentUser) return;

        try {
            await Database.addSession({
                userId: currentUser.uid,
                studyDate: this.startTimestamp || Date.now(),
                durationMinutes: this.duration / 60,
                sessionType: 'pomodoro',
                completedPomodoros: 1
            });
        } catch (e) {
            console.error('Error saving session:', e);
        }
    },

    playNotification() {
        try {
            const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACAf39/f3+AgICAgICAgH9/f39/gICAgICAgIB/f39/f4CAgICAgICAf39/f3+AgICAgICA');
            audio.play().catch(() => {});
        } catch (e) {}
    }
};
