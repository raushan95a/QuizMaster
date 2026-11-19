-- QuizMaster Lite database schema (SQLite).
-- Runs at every server start; IF NOT EXISTS makes it safe to run again.

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,
    salt          TEXT NOT NULL,
    full_name     TEXT NOT NULL,
    role          TEXT NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),
    created_at    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS quizzes (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    title          TEXT    NOT NULL,
    subject        TEXT    NOT NULL,
    description    TEXT,
    time_limit_min INTEGER NOT NULL CHECK (time_limit_min BETWEEN 1 AND 180),
    is_published   INTEGER NOT NULL DEFAULT 0,
    created_by     INTEGER NOT NULL REFERENCES users(id),
    created_at     TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS questions (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id        INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text  TEXT NOT NULL,
    option_a       TEXT NOT NULL,
    option_b       TEXT NOT NULL,
    option_c       TEXT NOT NULL,
    option_d       TEXT NOT NULL,
    correct_option TEXT NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D'))
);

CREATE TABLE IF NOT EXISTS attempts (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id          INTEGER NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    quiz_id          INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    status           TEXT    NOT NULL CHECK (status IN ('IN_PROGRESS', 'SUBMITTED')),
    started_at       TEXT    NOT NULL,
    submitted_at     TEXT,
    score            INTEGER,
    total            INTEGER,
    correct_count    INTEGER,
    wrong_count      INTEGER,
    unanswered_count INTEGER,
    time_taken_sec   INTEGER,
    auto_submitted   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS attempt_answers (
    attempt_id    INTEGER NOT NULL REFERENCES attempts(id)  ON DELETE CASCADE,
    question_id   INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    chosen_option TEXT CHECK (chosen_option IN ('A', 'B', 'C', 'D')),
    PRIMARY KEY (attempt_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_questions_quiz ON questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_attempts_quiz  ON attempts(quiz_id, status);
CREATE INDEX IF NOT EXISTS idx_attempts_user  ON attempts(user_id);
