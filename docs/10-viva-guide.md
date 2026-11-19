# 10 — Viva Guide

Short answers in simple words. For "show me in the code", open `05-syllabus-mapping.md` — every topic has a
clickable `file:line`.

## 1-minute project pitch
> QuizMaster is an online quiz system. A teacher creates quizzes in a JavaFX app; students take them from their own
> laptops with a countdown timer. Everything goes to a Java server (Javalin) that stores data in SQLite using JDBC,
> grades the answers, and pushes live results over WebSocket to a web page the faculty can open on a phone.
> It covers all five units: OOP and inheritance, polymorphism and exceptions, threads + file I/O + AWT events,
> JDBC + collections, and generics + JavaFX.

## Architecture in 4 sentences
1. **Two programs in one Maven project:** `ServerMain` (server) and `App` (JavaFX client).
2. **Client → server:** the app sends JSON over HTTP (`ApiClient`); the server never trusts the client for grading.
3. **Server layers:** controller (`web`) → business rules (`service`) → database code (`dao`) → SQLite.
4. **Live updates:** after every start/answer/submit the server builds a `LiveState` and pushes it over WebSocket
   (`LiveHub`) to the live page and the admin's Live Results screen.

## Demo order (6 minutes)
1. Server window: show it printing the Wi-Fi address.
2. Faculty open the live page on a phone.
3. Admin: create a 3-question, 2-minute quiz → publish.
4. 2–3 classmates take it → watch the live page and **● Live Results** update instantly.
5. Show a result + review, **Charts**, **Export CSV**, **F1** help window.
6. Open one syllabus topic per unit in the code.

---

## Unit 1 — OOP & inheritance
**Q: Why is `User` abstract?**
A plain "user" never exists — it is always an `Admin` or a `Student`. The abstract methods `getRole()` and
`getDashboardTitle()` force each subclass to give its own version.

**Q: Where do you use `super`?**
In `Admin`/`Student` constructors (`super(id, username, …)`) and in exceptions (`super("Invalid username or password")`).

**Q: Why are fields `protected` in `User` but `private` elsewhere?**
`protected` lets the subclasses use them directly; everything else is `private` with public getters (encapsulation).

**Q: Why is `PasswordUtil` a `final` class?**
It only has static helper methods; nobody should extend it and change how passwords are checked.

**Q: Interface vs abstract class here?**
`User` (abstract class) shares fields and code. `Repository<T>` and `Gradable` (interfaces) only describe what a
class can do — `Gradable` even gives a `default getPercentage()` method.

## Unit 2 — Polymorphism & exceptions
**Q: Show method overloading.**
`ApiClient.get(path, Class<T>)` and `get(path, TypeReference<T>)`; `AlertUtil.error(String)` and `error(String, Throwable)`.

**Q: Show constructor overloading.**
`Question()` (for JSON), `Question(text, a, b, c, d, correct)` (new), `Question(id, quizId, …)` (from the database).

**Q: Explain dynamic method dispatch in your project.**
The server's error handler has a `QuizAppException` reference and calls `e.getHttpStatus()`. At runtime Java runs
the method of the real object — `InvalidLoginException` returns 401, `NotFoundException` 404, and so on.
Same with `user.getDashboardTitle()` for `Admin` vs `Student`.

**Q: Checked or unchecked exceptions? Why?**
Checked (`extends Exception`) — the compiler forces every caller to handle or declare them, so no error is forgotten.

**Q: Where is a nested try with finally?**
`AttemptDAO.submit()`: outer try opens the connection; inner try runs the transaction and calls `rollback()` on
error; `finally` puts auto-commit back.

**Q: Why multiple catch blocks in `ApiClient`?**
`IOException` = server unreachable (show "Cannot reach the server"); `InterruptedException` = the thread was
cancelled (restore the interrupt flag). Different problems, different handling.

## Unit 3 — Threads, I/O, events
**Q: Which threads run during a quiz?**
1) JavaFX Application Thread (UI), 2) `QuizTimer` (countdown), 3) `AutoSaveThread` (saves answers),
4) a short background thread for each server call (`Async`).

**Q: Two ways to create a thread — show both.**
`QuizTimer implements Runnable` → `new Thread(this)`; `AutoSaveThread extends Thread` → overrides `run()`.

**Q: Explain `wait()` / `notify()` in your code.**
`AnswerQueue` is a producer–consumer buffer. The quiz screen (producer) calls `put()`, which adds the answer and
calls `notify()`. The auto-save thread (consumer) calls `take()`, which `wait()`s while the queue is empty.
Both methods are `synchronized`, because `wait`/`notify` need the object's lock.

**Q: What if the student clicks Submit at the same moment the timer hits zero?**
`submit()` is `synchronized` and uses an `AtomicBoolean` with `compareAndSet(false, true)` — only the first call
gets through. The server also only grades an attempt whose status is still `IN_PROGRESS`, so it is graded once.

**Q: Why `Platform.runLater`?**
JavaFX controls may only be changed on the JavaFX Application Thread. Background threads (timer, auto-save,
WebSocket) hand their updates to that thread with `Platform.runLater`.

**Q: Why `volatile` in `QuizTimer`?**
`running` is written by the UI thread (`stop()`) and read by the timer thread; `volatile` makes the change visible
immediately.

**Q: Where are byte streams used?**
Auto-save backup file: `DataOutputStream(new FileOutputStream(...))` to write, `DataInputStream(new FileInputStream(...))`
to read back. CSV export: `FileOutputStream` writing UTF-8 bytes.

**Q: Explain the delegation event model.**
An event **source** (button) creates an **event object** (`ActionEvent`) and sends it to the **listener** registered
with it (`addActionListener`). Shown in AWT `HelpWindow`; JavaFX's `setOnAction` works the same way.

**Q: Why use `WindowAdapter`?**
`WindowListener` has 7 methods. The adapter class implements all of them empty, so we override only
`windowClosing`. `KeyListener` (no adapter used) shows the difference: all 3 methods must be written.

## Unit 4 — JDBC & collections
**Q: Steps of JDBC in your project?**
`DriverManager.getConnection("jdbc:sqlite:data/quiz.db")` → `prepareStatement(sql)` → set parameters →
`executeQuery()` / `executeUpdate()` → read the `ResultSet` → close everything (try-with-resources).

**Q: Why `PreparedStatement` and not `Statement`?**
Values are sent separately from the SQL, so input like `' OR 1=1 --` can't change the query (prevents SQL injection).
It is also faster when repeated.

**Q: Why turn on `PRAGMA foreign_keys`?**
SQLite ignores foreign keys by default. Turning it on makes `ON DELETE CASCADE` work: deleting a quiz deletes its
questions and attempts.

**Q: Where is a transaction and why?**
Submitting: save answers + save the grade must happen together. `setAutoCommit(false)` … `commit()`, or
`rollback()` on error — never half-saved.

**Q: Which collections did you use and why?**
`ArrayList` (questions), `HashMap` (question id → answer), `HashSet` (one leaderboard row per student),
`Vector` (synchronized activity log), `ConcurrentHashMap` (logged-in sessions, live connections — used by many
threads), `Iterator` (cleaning answers), `Collections.shuffle` (random order).

**Q: `HashMap` vs `Hashtable` vs `ConcurrentHashMap`?**
`HashMap` is not thread-safe. `Hashtable` is thread-safe but locks the whole map for every call. `ConcurrentHashMap`
is thread-safe and lets many threads work at once — so the server uses it.

## Unit 5 — Generics & JavaFX
**Q: Show a generic class with two type parameters.**
`Pair<K, V>` — used as `Pair<String, Integer>` = ("40-60 %", 3) for the bar chart.

**Q: What does `List<? extends Gradable>` mean?**
"A list of any type that is a `Gradable`" — so `ScoreStats.of()` accepts `List<ResultDto>` and `List<Attempt>`.

**Q: Why a generic `Repository<T>`?**
All DAOs need the same 5 operations; the interface is written once and each DAO fills in its type:
`UserDAO implements Repository<User>`.

**Q: JavaFX application skeleton?**
`App extends Application` → `start(Stage)` builds the first scene → `launch(args)`. `Launcher` is a plain class with
`main` so the app also starts from a normal jar.

**Q: Which controls and effects did you use?**
`TextField`, `PasswordField`, `RadioButton` + `ToggleGroup`, `ListView`, `ComboBox`, `TableView`, `Spinner`,
`ProgressBar`, `MenuBar`/`Menu`/`MenuItem` with images, `ToolBar`, `BarChart`, `PieChart`; `DropShadow` effect;
`FadeTransition` + `ScaleTransition` on the score.

## Design & security questions
**Q: Why is grading on the server, not in the app?**
The app is on the student's laptop and could be changed. The server never sends correct answers before submit,
and it checks the time limit itself.

**Q: How are passwords stored?**
Never in plain text. PBKDF2 with a random salt per user (210 000 rounds); login compares hashes with a
constant-time check.

**Q: What if the internet drops during a quiz?**
Answers are still saved in a local backup file; the screen shows "Offline". When the app reopens the quiz,
it resumes the same attempt with the saved answers and the remaining time.

**Q: How does the live page update without refreshing?**
The browser keeps a WebSocket open. After each change the server pushes the new state, and the page redraws.
If the connection drops it reconnects every 3 seconds.

**Q: How did you test it?**
44 automatic JUnit tests (passwords, login, quiz rules, grading, time limits, resume, live updates, CSV, statistics,
`wait`/`notify`) plus manual end-to-end runs. `.\mvnw.cmd test` runs them all.

**Q: What would you add next?**
Online hosting, importing questions from CSV, and generating a quiz automatically from a unit's PPT.
