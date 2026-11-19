# 01 — Project Overview

## Problem
Paper quizzes are slow to conduct and grade. **QuizMaster Lite** lets a teacher create a quiz once; students take it
from their own computers with a timer; a central server grades it instantly and stores results; anyone with the
live link sees the leaderboard update in real time.

## Parts
| Part | Who uses it | What it does |
|---|---|---|
| JavaFX app | Admin, students | Manage quizzes, take quizzes, see results |
| Java server (hosted online) | The app + live page | Login, database (SQLite via JDBC), grading |
| Live page (`/live.html`) | Faculty / audience | Read-only leaderboard + who is writing, updates instantly (WebSocket) |

## Roles
| Role | Can do |
|---|---|
| Admin | Create/edit/delete quizzes and questions, publish/unpublish, see live results, export CSV |
| Student | Register, take published quizzes, see result + review, see own history |
| Viewer (no login) | Open the live page for a quiz |

## Functional requirements
**Login**
- FR1 Students register (full name, username, password ≥ 6 chars). Duplicate username → error.
- FR2 Login with username + password; wrong details → "Invalid username or password".
- FR3 After login the user sees the Admin or Student dashboard; Logout returns to login.

**Admin**
- FR4 Create, edit, delete quizzes (title, subject, description, time limit in minutes).
- FR5 Add, edit, delete questions (4 options, one correct).
- FR6 Publish (needs ≥ 1 question) / unpublish; students only see published quizzes.
- FR7 Live Results screen: who is writing (answered count) and submitted scores, updated instantly over WebSocket.
- FR8 Export a quiz's results to a CSV file.

**Student**
- FR9 See published quizzes (subject, number of questions, time limit).
- FR10 Take a quiz: one question at a time, Next/Previous, change answers, keys 1–4 and ←/→.
- FR11 Countdown timer; at 00:00 the quiz is submitted automatically.
- FR12 Questions are shuffled for each attempt.
- FR13 Answers auto-save in the background (to the server for live progress, and to a local backup file).
- FR14 After submit: score, percentage, correct/wrong/unanswered, then a review of every question.
- FR15 History of own attempts.

**Live page**
- FR16 `/live.html?quiz=<id>` shows quiz title, students writing (with progress), and the leaderboard; updates instantly over WebSocket and reconnects automatically; works on phones.

**Help**
- FR17 Help → Keyboard Shortcuts opens a small AWT window.

## Non-functional
- Passwords stored as PBKDF2 hashes with a random salt; SQL only through `PreparedStatement`.
- Correct answers are never sent to the student before submitting; grading happens on the server.
- Server rejects a submission arriving more than 30 s after the time limit.
- The UI never freezes (network and file work run on background threads).
- Hosted online with a persistent disk so data survives restarts.

## Feature tiers (committed scope)
**Basic — must be 100% working**
- [ ] Server online + JavaFX app connected to it
- [ ] Login / register with roles
- [ ] Admin: quizzes and questions (create/edit/delete)
- [ ] Student: take a quiz, server grades it, score shown
- [ ] Data stored in SQLite via JDBC

**Advanced — committed**
- [ ] Timer thread with auto-submit
- [ ] Auto-save thread (wait/notify) + local backup file
- [ ] Shuffled questions, review screen, history
- [ ] Live page + admin Live Results (WebSocket, real-time)
- [ ] Publish / unpublish
- [ ] CSV export
- [ ] AWT Help window, JavaFX effects, menus with icons

**Outstanding — stretch goals**
- [ ] Charts (score distribution)
- [ ] Import questions from CSV
- [ ] Dark / light theme
- [ ] QR code of the live link
