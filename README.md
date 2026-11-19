# QuizMaster Lite — Online Quiz / Exam System

A JavaFX quiz app connected to a Java server (Javalin + JDBC + SQLite), with a live results page that faculty
can open on any phone. Course project for **Advanced Java Programming (CSE 307/309), SRM University AP**.

```
JavaFX app (admin / students) ──HTTP──► Java server ──JDBC──► SQLite
Faculty phone ──► http://<server>/live.html   (instant updates via WebSocket)
```

## Features
- Login / register with roles (Admin, Student), passwords hashed with PBKDF2
- **Admin:** create quizzes and questions, publish, watch results live, charts, export results to CSV
- **Student:** timed quiz (auto-submit at 0:00), background auto-save, resume after a crash,
  instant result with animation, answer review, result history
- **Live page** for faculty: leaderboard + who is writing, updates instantly, works on phones
- Keyboard shortcuts (1–4, ← / →) and an F1 help window (AWT)

## How to run (Windows)

**Needs:** Java 17 or newer (`java -version`). Maven is *not* needed — `mvnw.cmd` downloads it.
Open a terminal (PowerShell or the VS Code terminal) in the project folder.

**1. Build** — first time, and after changing code (first build takes 1–2 minutes):
```
.\mvnw.cmd clean package
```
Wait for `BUILD SUCCESS`.

**2. Start the server** — terminal 1, keep it open:
```
java -jar target\quizmaster-server.jar
```
Ready when it prints `QuizMaster server running on port 7070`.

**3. Start the app** — terminal 2:
```
.\mvnw.cmd javafx:run
```
Run the same command in another terminal to open a second window (e.g. one admin + one student).

**4. Use it**
| What | How |
|---|---|
| Admin login | `admin` / `admin123` |
| Student | click **"New student? Create an account"** on the login screen |
| Sample quiz | **"Java Basics"** (5 questions) is ready to take |
| Live page | open `http://localhost:7070/live.html` in a browser |
| Help | press **F1** after logging in |

**5. Stop** — close the app windows, then press `Ctrl + C` in the server terminal.

### Demo on several devices (same Wi-Fi, no internet needed)
The server prints this laptop's network address, e.g.
`On this network: app -Dserver.url=http://10.7.137.162:7070   live page: http://10.7.137.162:7070/live.html`
- Faculty phone: open the **live page** address.
- Classmates' laptops: `.\mvnw.cmd javafx:run "-Dserver.url=http://10.7.137.162:7070"`
- If others can't connect, allow Java through Windows Firewall (Private networks).

### Troubleshooting
| Problem | Fix |
|---|---|
| App says "Cannot reach the server" | Start the server first (step 2) |
| `Address already in use` | A server is already running — close it first |
| `'mvnw.cmd' is not recognized` | Type `.\mvnw.cmd` (with `.\`) |
| Want a fresh start | Stop the server and delete the `data` folder |

### Tests
```
.\mvnw.cmd test
```
44 automatic tests (login, quiz rules, grading, timer limits, resume, live updates, CSV, charts, threads).

## Tech
Java 17 · JavaFX 21 · Javalin 6 (HTTP + WebSocket) · SQLite with plain JDBC · Jackson · Maven · JUnit 5

## Documentation
[Overview](docs/01-overview.md) · [Architecture](docs/02-architecture.md) · [Database](docs/03-database.md) ·
[Screens](docs/04-ui-screens.md) · [Syllabus mapping](docs/05-syllabus-mapping.md) · [Roadmap](docs/06-roadmap.md) ·
[Setup](docs/07-setup.md) · [API](docs/08-api.md) · [Deployment](docs/09-deployment.md) ·
[Viva guide](docs/10-viva-guide.md)
