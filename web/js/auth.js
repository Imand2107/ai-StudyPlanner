// Authentication module
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

async function login() {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        showAuthError('Please fill in all fields');
        return;
    }

    showAuthLoader(true);
    try {
        const result = await auth.signInWithEmailAndPassword(email, password);
        currentUser = result.user;
        localStorage.setItem('studyflow_user', JSON.stringify({
            uid: currentUser.uid,
            email: currentUser.email
        }));
    } catch (error) {
        showAuthError(getAuthErrorMessage(error.code));
    }
    showAuthLoader(false);
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
    try {
        const result = await auth.createUserWithEmailAndPassword(email, password);
        currentUser = result.user;
        await Database.createUser(currentUser.uid, { name, email });
        localStorage.setItem('studyflow_user', JSON.stringify({
            uid: currentUser.uid,
            email: currentUser.email,
            name
        }));
    } catch (error) {
        showAuthError(getAuthErrorMessage(error.code));
    }
    showAuthLoader(false);
}

async function resetPassword() {
    const email = document.getElementById('forgot-email').value.trim();
    if (!email) {
        showAuthError('Please enter your email');
        return;
    }

    showAuthLoader(true);
    try {
        await auth.sendPasswordResetEmail(email);
        showToast('Password reset email sent!', 'success');
        showLogin();
    } catch (error) {
        showAuthError(getAuthErrorMessage(error.code));
    }
    showAuthLoader(false);
}

function logout() {
    auth.signOut();
    currentUser = null;
    localStorage.removeItem('studyflow_user');
    document.getElementById('auth-screen').classList.add('active');
    document.getElementById('app-screen').classList.remove('active');
}

function getAuthErrorMessage(code) {
    const messages = {
        'auth/user-not-found': 'No account found with this email',
        'auth/wrong-password': 'Incorrect password',
        'auth/email-already-in-use': 'An account with this email already exists',
        'auth/invalid-email': 'Invalid email address',
        'auth/weak-password': 'Password is too weak',
        'auth/too-many-requests': 'Too many attempts. Please try again later',
        'auth/network-request-failed': 'Network error. Check your connection'
    };
    return messages[code] || 'An error occurred. Please try again';
}

// Listen for auth state changes
auth.onAuthStateChanged(async (user) => {
    if (user) {
        currentUser = user;
        const userData = await Database.getUser(user.uid);
        const userName = userData?.name || user.email.split('@')[0];

        document.getElementById('auth-screen').classList.remove('active');
        document.getElementById('app-screen').classList.add('active');
        document.getElementById('sidebar-user-name').textContent = userName;
        document.getElementById('sidebar-user-email').textContent = user.email;

        localStorage.setItem('studyflow_user', JSON.stringify({
            uid: user.uid,
            email: user.email,
            name: userName
        }));

        navigateTo('dashboard');
    }
});
