# 06 — Roadmap & Progress

Rubric (A4. Project — 15 marks): idea & features 3–5 · partial 4–10 · full committed features 10–15 · outstanding → full marks.
Finish the four steps completely before any stretch goal. Tick items only when they work end-to-end.

Online deployment is postponed; the demo runs on a local server over Wi-Fi (see `07-setup.md`).

## Step 1 — Server, database, login
- [x] Maven project + wrapper, `.gitignore`, GitHub repo
- [x] Models (`User`, `Admin`, `Student`, `Role`), DTOs, exceptions
- [x] `DatabaseManager` + `schema.sql` + admin seeding
- [x] `PasswordUtil` (PBKDF2), `SessionStore`, `UserDAO` (`Repository<T>`), `AuthService` + tests
- [x] Server: `/health`, register, login, logout, me; error handling
- [x] Client: `ApiClient`, `Session`, Login + Register screens, placeholder dashboards with logout
- [x] Dockerfile (used by the host automatically)
- [ ] Deploy server online (postponed — later)

## Step 2 — Admin
- [x] `QuizDAO`, `QuestionDAO`, `QuizService` + admin endpoints + tests
- [x] Sample quiz "Java Basics" seeded in dev
- [x] Admin Dashboard (`ToolBar`, `TableView`)
- [x] Quiz Editor + question dialog; publish / unpublish

## Step 3 — Student
- [x] Start attempt (shuffled, no answers sent), progress, submit + grading (transaction) + tests
- [x] Student Dashboard, Quiz screen, `QuizTimer`, keys 1–4 / ←/→
- [x] `AnswerQueue` + `AutoSaveThread` (wait/notify, backup file)
- [x] Result (animation), Review, History
- [x] Resume an unfinished attempt (same question order, saved answers, remaining time)

## Step 4 — Live + polish
- [x] `LiveHub` WebSocket `/ws/live` + `GET /api/live/{quizId}` + `live.html` (phone + projector layout)
- [x] Admin Live Results (`LiveClient`, activity log) — end-to-end test passes; check the screen by hand once
- [x] CSV export, AWT Help window (F1), menus with image icons, `DropShadow`, CSS polish
- [x] Server prints its Wi-Fi address for the local demo
- [ ] Fill exact line references in `05-syllabus-mapping.md` (before the viva)

## Stretch
- [ ] Charts · CSV question import · dark theme · QR code for the live link
- [ ] Auto-generate quiz from a unit PPT (Apache POI + AI → draft quiz for faculty review) — see `01-overview.md`

## Pre-demo checklist
- [ ] `https://<server>/health` shows `UP`; DB backup downloaded
- [ ] Register a student from a second laptop; duplicate username and wrong password show errors
- [ ] Admin creates and publishes a quiz; live link opened on a phone
- [ ] 2–3 students take it together → live page and Live Results update instantly
- [ ] Timer reaching 0 auto-submits
- [ ] Result, review, history, CSV export correct; AWT Help opens
- [ ] No freezes, no stack traces

## Demo script (~6 min)
1. Architecture in one line: JavaFX app ⇄ online Java server ⇄ SQLite; live page over WebSocket.
2. Faculty open the live link on their phone.
3. Admin creates a 3-question, 2-minute quiz and publishes it.
4. Classmates take it on laptops; everyone watches the live page fill in.
5. Show result + review, CSV export, and one syllabus topic per unit in the code.

Backup plan: no internet → run the server on a laptop, connect laptops via a phone hotspot,
start clients with `-Dserver.url=http://<laptop-ip>:7070`.
