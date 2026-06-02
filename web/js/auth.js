// Authentication module (localStorage-based)
let currentUser = null;

function showLogin() {
    document.getElementById('login-form').classList.add('active');
    document.getElementById('register-form').classList.remove('active');
    document.getElementById('forgot-form').classList.remove('active');
    clearErrors();
}

function showRegister() {
    document.getElementById('login-form').classList.remove('active');
    document.getElementById('register-form').classList.add('active');
    document.getElementById('forgot-form').classList.remove('active');
    clearErrors();
}

function showForgotPassword() {
    document.getElementById('login-form').classList.remove('active');
    document.getElementById('register-form').classList.remove('active');
    document.getElementById('forgot-form').classList.add('active');
    clearErrors();
}

function clearErrors() {
    document.getElementById('auth-error').classList.add('hidden');
}

function showAuthError(msg) {
    const el = document.getElementById('auth-error');
    el.textContent = msg;
    el.classList.remove('hidden');
}

function showAuthLoader(show) {
    document.getElementById('auth-loader').classList.toggle('hidden', !show);
}

function getStoredUsers() {
    const data = localStorage.getItem('studyflow_users');
    return data ? JSON.parse(data) : {};
}

function saveStoredUsers(users) {
    localStorage.setItem('studyflow_users', JSON.stringify(users));
}

async function login() {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        showAuthError('Please fill in all fields');
        return;
    }

    showAuthLoader(true);

    // Simulate network delay
    await new Promise(r => setTimeout(r, 500));

    const users = getStoredUsers();
    const user = users[email];

    if (!user) {
        showAuthError('No account found with this email');
        showAuthLoader(false);
        return;
    }

    if (user.password !== password) {
        showAuthError('Incorrect password');
        showAuthLoader(false);
        return;
    }

    currentUser = { uid: user.uid, email: user.email, name: user.name };
    localStorage.setItem('studyflow_current_user', JSON.stringify(currentUser));
    showAuthLoader(false);
    enterApp();
}

async function register() {
    const name = document.getElementById('register-name').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const password = document.getElementById('register-password').value;
    const confirm = document.getElementById('register-confirm').value;

    if (!name || !email || !password || !confirm) {
        showAuthError('Please fill in all fields');
        return;
    }

    if (password.length < 6) {
        showAuthError('Password must be at least 6 characters');
        return;
    }

    if (password !== confirm) {
        showAuthError('Passwords do not match');
        return;
    }

    showAuthLoader(true);
    await new Promise(r => setTimeout(r, 500));

    const users = getStoredUsers();

    if (users[email]) {
        showAuthError('An account with this email already exists');
        showAuthLoader(false);
        return;
    }

    const uid = generateId();
    users[email] = { uid, email, name, password };
    saveStoredUsers(users);

    await Database.createUser(uid, { name, email });

    currentUser = { uid, email, name };
    localStorage.setItem('studyflow_current_user', JSON.stringify(currentUser));
    showAuthLoader(false);
    showToast('Account created successfully!', 'success');
    enterApp();
}

function resetPassword() {
    const email = document.getElementById('forgot-email').value.trim();
    if (!email) {
        showAuthError('Please enter your email');
        return;
    }

    const users = getStoredUsers();
    if (!users[email]) {
        showAuthError('No account found with this email');
        return;
    }

    showToast('Password reset is not available in demo mode', 'info');
    showLogin();
}

function logout() {
    currentUser = null;
    localStorage.removeItem('studyflow_current_user');
    document.getElementById('auth-screen').classList.add('active');
    document.getElementById('app-screen').classList.remove('active');
}

function enterApp() {
    document.getElementById('auth-screen').classList.remove('active');
    document.getElementById('app-screen').classList.add('active');
    document.getElementById('sidebar-user-name').textContent = currentUser.name || currentUser.email.split('@')[0];
    document.getElementById('sidebar-user-email').textContent = currentUser.email;
    navigateTo('dashboard');
}

// Check for existing session on load
function checkAuth() {
    const stored = localStorage.getItem('studyflow_current_user');
    if (stored) {
        try {
            currentUser = JSON.parse(stored);
            if (currentUser && currentUser.uid) {
                enterApp();
                return;
            }
        } catch (e) {}
    }
    document.getElementById('auth-screen').classList.add('active');
}

// Init auth on DOM ready
document.addEventListener('DOMContentLoaded', checkAuth);
