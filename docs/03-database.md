# 03 — Database (SQLite, on the server)

- File: `DB_PATH` env var (dev `data/quiz.db`, production `/data/quiz.db` on a persistent volume)
- JDBC URL: `jdbc:sqlite:<DB_PATH>` with `DriverManager`; `PRAGMA foreign_keys = ON` on each connection
- Schema: `src/main/resources/schema.sql`, run at every startup (`CREATE TABLE IF NOT EXISTS`)
- Timestamps: ISO-8601 UTC strings made by the server

## Tables
```
users 1──< quizzes 1──< questions
users 1──< attempts >──1 quizzes
attempts 1──< attempt_answers >──1 questions
```

```sql
CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,   -- PBKDF2, Base64
    salt          TEXT NOT NULL,   -- random 16 bytes, Base64
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
```

## Seed data
- Dev (`APP_ENV=dev`, default): admin `admin` / `admin123`.
- Production: admin from `ADMIN_USERNAME` / `ADMIN_PASSWORD` env vars (server will not start without them on an empty DB).
- A sample quiz "Java Basics" is added in step 2.

## Key queries
```sql
-- Leaderboard (best score per student, then fastest)
SELECT u.full_name, MAX(a.score) AS best, a.total, MIN(a.time_taken_sec) AS time
FROM attempts a JOIN users u ON u.id = a.user_id
WHERE a.quiz_id = ? AND a.status = 'SUBMITTED'
GROUP BY a.user_id ORDER BY best DESC, time ASC;

-- Students currently writing, with answered count
SELECT u.full_name, COUNT(aa.chosen_option) AS answered
FROM attempts a JOIN users u ON u.id = a.user_id
LEFT JOIN attempt_answers aa ON aa.attempt_id = a.id
WHERE a.quiz_id = ? AND a.status = 'IN_PROGRESS'
GROUP BY a.id;
```
Submitting saves answers + grades + updates the attempt in **one transaction** (commit / rollback).

## Backup
Before the demo, download `/data/quiz.db` from the host (see `09-deployment.md`).
