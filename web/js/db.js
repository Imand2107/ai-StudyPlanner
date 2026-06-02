// Firebase Configuration - Replace with your config
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "YOUR_PROJECT.firebaseapp.com",
    projectId: "YOUR_PROJECT_ID",
    storageBucket: "YOUR_PROJECT.appspot.com",
    messagingSenderId: "YOUR_SENDER_ID",
    appId: "YOUR_APP_ID"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();

// Database operations
const Database = {
    // User operations
    async createUser(userId, data) {
        return db.collection('users').doc(userId).set({
            ...data,
            createdAt: firebase.firestore.FieldValue.serverTimestamp()
        });
    },

    async getUser(userId) {
        const doc = await db.collection('users').doc(userId).get();
        return doc.exists ? doc.data() : null;
    },

    // Subject operations
    async addSubject(data) {
        const ref = db.collection('subjects').doc();
        await ref.set({ ...data, subjectId: ref.id, createdAt: Date.now() });
        return ref.id;
    },

    async getSubjects(userId) {
        const snapshot = await db.collection('subjects')
            .where('userId', '==', userId)
            .get();
        return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    },

    async updateSubject(subjectId, data) {
        return db.collection('subjects').doc(subjectId).update(data);
    },

    async deleteSubject(subjectId) {
        return db.collection('subjects').doc(subjectId).delete();
    },

    // Task operations
    async addTask(data) {
        const ref = db.collection('tasks').doc();
        await ref.set({ ...data, taskId: ref.id, createdAt: Date.now() });
        return ref.id;
    },

    async getTasks(userId) {
        const snapshot = await db.collection('tasks')
            .where('userId', '==', userId)
            .get();
        return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    },

    async updateTask(taskId, data) {
        return db.collection('tasks').doc(taskId).update(data);
    },

    async deleteTask(taskId) {
        return db.collection('tasks').doc(taskId).delete();
    },

    // Study Session operations
    async addSession(data) {
        const ref = db.collection('study_sessions').doc();
        await ref.set({ ...data, sessionId: ref.id });
        return ref.id;
    },

    async getSessions(userId) {
        const snapshot = await db.collection('study_sessions')
            .where('userId', '==', userId)
            .get();
        return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    }
};
