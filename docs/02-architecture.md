# 02 — Architecture

## Overview
```
┌──────────────────────┐   HTTP + JSON    ┌───────────────────────────────┐
│ JavaFX app           │ ───────────────► │ Java server (Javalin)         │
│  ui → ApiClient      │ ◄─────────────── │  web → service → dao ─JDBC─►  │──► SQLite file
│  threads: timer,     │                  │  LiveHub (WebSocket) +        │
│  auto-save           │ ◄── WebSocket ── │  static files: live.html      │
└──────────────────────┘                  └───────────────┬───────────────┘
                                                          │ WebSocket /ws/live?quiz=<id> (pushed on every change)
                                          Faculty browser ◄┘
```
One Maven project builds both programs. The server jar is deployed online; the app runs on each PC.

## Folder structure
```
java Project/
├── pom.xml, mvnw, mvnw.cmd, .mvn/          Maven + wrapper
├── Dockerfile, .dockerignore, .gitignore
├── docs/
├── data/                                    quiz.db (local dev, git-ignored)
├── reference/                               your syllabus PDF + rubric (git-ignored)
└── src/
    ├── main/java/com/quizapp/
    │   ├── model/       Role, User (abstract), Admin, Student, Quiz, Question, Attempt, Gradable
    │   ├── dto/         records: LoginRequest, RegisterRequest, AuthResponse, UserDto, ErrorResponse, …
    │   ├── exception/   QuizAppException, ValidationException, InvalidLoginException,
    │   │                DuplicateUserException, UnauthorizedException, ForbiddenException, NotFoundException
    │   ├── server/
    │   │   ├── ServerMain.java       creates Javalin, routes, error handling, starts server
    │   │   ├── Config.java           reads env vars
    │   │   ├── DatabaseManager.java  JDBC connections, runs schema.sql, seeds admin
    │   │   ├── PasswordUtil.java     PBKDF2WithHmacSHA256 + salt
    │   │   ├── SessionStore.java     token → user id (in memory)
    │   │   ├── dao/                  Repository<T>, UserDAO, QuizDAO, QuestionDAO, AttemptDAO
    │   │   ├── service/              AuthService, QuizService, AttemptService
    │   │   └── web/                  AuthController, AdminController, StudentController, LiveHub (WebSocket)
    │   └── client/
    │       ├── App.java              JavaFX Application
    │       ├── Launcher.java         plain main() → App
    │       ├── ClientConfig.java     server URL
    │       ├── ApiClient.java        HttpClient + Jackson
    │       ├── LiveClient.java       java.net.http WebSocket (admin Live Results)
    │       ├── Session.java          logged-in user + token
    │       ├── ui/                   SceneManager + one class per screen
    │       ├── thread/               QuizTimer, AnswerQueue, AutoSaveThread
    │       ├── awt/                  HelpWindow
    │       └── util/                 Async, AlertUtil, ResultExporter
    ├── main/resources/
    │   ├── schema.sql
    │   ├── public/                   index.html, live.html (served by the server)
    │   └── client/styles.css
    └── test/java/com/quizapp/…
```

## Class design
```
        «abstract» User (id, username, fullName, passwordHash, salt)
        + getRole() : Role            (abstract)
        + getDashboardTitle()         (abstract)
        + static create(Role, …)      → Admin or Student
             ▲              ▲
           Admin          Student

«interface» Repository<T>                 «interface» Gradable
 add(T) : int                              calculateScore(), getPercentage()
 findById(int) : Optional<T>                     ▲
 findAll() : List<T>                          Attempt
 update(T), delete(int)
     ▲
UserDAO, QuizDAO, QuestionDAO, AttemptDAO

QuizAppException (extends Exception)  + getHttpStatus() → 500
 ├─ ValidationException      400       ├─ UnauthorizedException 401
 ├─ InvalidLoginException    401       ├─ ForbiddenException    403
 ├─ DuplicateUserException   409       └─ NotFoundException     404
```
Each exception overrides `getHttpStatus()` — the server's error handler just calls it (dynamic method dispatch).

## Flows
**Login**
```
LoginView → Async → ApiClient.post("/api/auth/login") → AuthController → AuthService
   → UserDAO.findByUsername → PasswordUtil.verify → SessionStore.create(userId)
   ← {token, user} → Session.start(...) → SceneManager shows Admin/Student dashboard
```
**Take a quiz**
```
StudentDashboard → POST /api/quizzes/{id}/start → attemptId, timeLimitSec, shuffled questions (no answers)
QuizView: QuizTimer thread ticks every second (Platform.runLater updates label; at 0 → submit)
          option chosen → AnswerQueue.put()  ─►  AutoSaveThread.take()
                                                   ├─ PUT /api/attempts/{id}/progress (live progress)
                                                   └─ writes ~/.quizmaster/autosave/attempt-<id>.dat (FileOutputStream)
submit (button or timer, exactly once) → POST /api/attempts/{id}/submit → server grades → ResultView → ReviewView
```
**Live (WebSocket)**
```
live.html (browser) / LiveClient (admin app) ── connect ──► /ws/live?quiz=<id>
server: LiveHub keeps ConcurrentHashMap<quizId, Set<WsContext>>
        on connect            → sends current LiveState {quizTitle, writing[], leaderboard[]}
        after start/progress/submit commits → builds LiveState once, sends to every subscriber of that quiz
browser: re-renders; on disconnect retries every 3 s.   admin app: Platform.runLater → update tables
```
LiveState contains only names, answered counts, scores and times — never questions or answers.
Fallback: `GET /api/live/{quizId}` returns the same LiveState as JSON.

## Threads (client)
| Thread | How | Job |
|---|---|---|
| JavaFX Application Thread | — | UI only |
| `QuizTimer` | `implements Runnable` | countdown, auto-submit |
| `AutoSaveThread` | `extends Thread` | consumer of `AnswerQueue` (`wait()`/`notify()`), saves progress |
| WebSocket listener | `java.net.http` internal thread | `LiveClient` receives live state → `Platform.runLater` |
| `Async` tasks | background thread per request | login, loading lists, submit |

Background threads touch the UI only through `Platform.runLater`. Submit is guarded by a `synchronized` method + flag.

## Sessions & security (simple)
- Passwords: `PBKDF2WithHmacSHA256` (built into Java), 16-byte random salt, 210 000 iterations, stored Base64.
- Login returns a random UUID token; server keeps `token → userId` in a `ConcurrentHashMap` (`SessionStore`).
  Restarting the server logs everyone out — acceptable.
- Client sends `Authorization: Bearer <token>`. A `before("/api/*")` handler resolves the user;
  admin routes check `role == ADMIN`.
- Public endpoints: `/health`, `/api/auth/login`, `/api/auth/register`, `/api/live/*`, `/ws/live`, static files
  (live data is read-only and contains no answers).
