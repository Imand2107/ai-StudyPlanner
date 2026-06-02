// LocalStorage Database (works without Firebase)
const DB_KEY = 'studyflow_db';

function getDB() {
    const data = localStorage.getItem(DB_KEY);
    return data ? JSON.parse(data) : { users: {}, subjects: {}, tasks: {}, sessions: {} };
}

function saveDB(db) {
    localStorage.setItem(DB_KEY, JSON.stringify(db));
}

function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2, 9);
}

// Database operations (localStorage)
const Database = {
    async createUser(userId, data) {
        const db = getDB();
        db.users[userId] = { ...data, createdAt: Date.now() };
        saveDB(db);
    },

    async getUser(userId) {
        const db = getDB();
        return db.users[userId] || null;
    },

    async addSubject(data) {
        const db = getDB();
        const id = generateId();
        db.subjects[id] = { ...data, subjectId: id, createdAt: Date.now() };
        saveDB(db);
        return id;
    },

    async getSubjects(userId) {
        const db = getDB();
        return Object.values(db.subjects).filter(s => s.userId === userId);
    },

    async updateSubject(subjectId, data) {
        const db = getDB();
        if (db.subjects[subjectId]) {
            db.subjects[subjectId] = { ...db.subjects[subjectId], ...data };
            saveDB(db);
        }
    },

    async deleteSubject(subjectId) {
        const db = getDB();
        delete db.subjects[subjectId];
        saveDB(db);
    },

    async addTask(data) {
        const db = getDB();
        const id = generateId();
        db.tasks[id] = { ...data, taskId: id, createdAt: Date.now() };
        saveDB(db);
        return id;
    },

    async getTasks(userId) {
        const db = getDB();
        return Object.values(db.tasks).filter(t => t.userId === userId);
    },

    async updateTask(taskId, data) {
        const db = getDB();
        if (db.tasks[taskId]) {
            db.tasks[taskId] = { ...db.tasks[taskId], ...data };
            saveDB(db);
        }
    },

    async deleteTask(taskId) {
        const db = getDB();
        delete db.tasks[taskId];
        saveDB(db);
    },

    async addSession(data) {
        const db = getDB();
        const id = generateId();
        db.sessions[id] = { ...data, sessionId: id };
        saveDB(db);
        return id;
    },

    async getSessions(userId) {
        const db = getDB();
        return Object.values(db.sessions).filter(s => s.userId === userId);
    }
};
