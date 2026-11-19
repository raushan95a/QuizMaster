# 05 — Syllabus Mapping

Where every syllabus topic is in the code — use it in the viva to open the exact line.
All paths are under `src/main/java/com/quizapp/`; each link opens the file at that line (on GitHub or in the IDE).
Line numbers are from the final version; if you edit a file, re-check its numbers.

## Unit 1 — Overview of Java and Inheritance
| Topic | Where (file:line) | What to say |
|---|---|---|
| Arrays | [Question.java:10](../src/main/java/com/quizapp/model/Question.java#L10), [QuizView.java:58](../src/main/java/com/quizapp/client/ui/QuizView.java#L58) | `char[] OPTIONS`, `RadioButton[] optionButtons` |
| Control statements | [Question.java:47](../src/main/java/com/quizapp/model/Question.java#L47) (`switch`), [AttemptService.java:158](../src/main/java/com/quizapp/server/service/AttemptService.java#L158) (`for` + `if/else` grading) | grading loop counts correct / wrong / unanswered |
| Classes, objects, constructors | [Question.java:23](../src/main/java/com/quizapp/model/Question.java#L23), [Quiz.java:29](../src/main/java/com/quizapp/model/Quiz.java#L29) | plain model classes |
| `this` reference | [Question.java:29](../src/main/java/com/quizapp/model/Question.java#L29) | `this.text = text` |
| `static` | [Config.java:26](../src/main/java/com/quizapp/server/Config.java#L26), [User.java:33](../src/main/java/com/quizapp/model/User.java#L33) | `Config.fromEnvironment()`, static factory `User.create()` |
| `final` (constants) | [Config.java:8](../src/main/java/com/quizapp/server/Config.java#L8), [AttemptService.java:35](../src/main/java/com/quizapp/server/service/AttemptService.java#L35), [PasswordUtil.java:17](../src/main/java/com/quizapp/server/PasswordUtil.java#L17) | `DEFAULT_PORT`, `GRACE_SECONDS`, `ITERATIONS` |
| `final` with inheritance | [PasswordUtil.java:14](../src/main/java/com/quizapp/server/PasswordUtil.java#L14) | `final class` — cannot be extended |
| Inheritance | [Admin.java:4](../src/main/java/com/quizapp/model/Admin.java#L4), [Student.java:4](../src/main/java/com/quizapp/model/Student.java#L4) | both `extends User` |
| Member access (`protected`) | [User.java:9](../src/main/java/com/quizapp/model/User.java#L9) | fields visible to subclasses |
| Abstract class | [User.java:6](../src/main/java/com/quizapp/model/User.java#L6), [User.java:24](../src/main/java/com/quizapp/model/User.java#L24) | `abstract Role getRole()` |
| `super` | [Admin.java:7](../src/main/java/com/quizapp/model/Admin.java#L7), [InvalidLoginException.java:7](../src/main/java/com/quizapp/exception/InvalidLoginException.java#L7) | calls the parent constructor |
| Multilevel hierarchy | [QuizAppException.java:8](../src/main/java/com/quizapp/exception/QuizAppException.java#L8) | `Exception` → `QuizAppException` → `InvalidLoginException` |
| Interface | [Repository.java:13](../src/main/java/com/quizapp/server/dao/Repository.java#L13), [Gradable.java:4](../src/main/java/com/quizapp/model/Gradable.java#L4) | implemented by DAOs / `Attempt` / `ResultDto` |
| Default method in interface | [Gradable.java:11](../src/main/java/com/quizapp/model/Gradable.java#L11) | `default getPercentage()` |
| Packages, access specifiers | folders `model`, `dto`, `exception`, `server`, `client` | `private` fields + `public` getters everywhere |

## Unit 2 — Polymorphism and Exception Handling
| Topic | Where (file:line) | What to say |
|---|---|---|
| Method overloading | [ApiClient.java:38](../src/main/java/com/quizapp/client/ApiClient.java#L38) / [:42](../src/main/java/com/quizapp/client/ApiClient.java#L42), [ApiClient.java:48](../src/main/java/com/quizapp/client/ApiClient.java#L48) / [:53](../src/main/java/com/quizapp/client/ApiClient.java#L53), [AlertUtil.java:12](../src/main/java/com/quizapp/client/util/AlertUtil.java#L12) / [:17](../src/main/java/com/quizapp/client/util/AlertUtil.java#L17) | same name, different parameters |
| Constructor overloading | [Question.java:23](../src/main/java/com/quizapp/model/Question.java#L23), [:27](../src/main/java/com/quizapp/model/Question.java#L27), [:38](../src/main/java/com/quizapp/model/Question.java#L38) | no-arg / new question / loaded from DB |
| Method overriding | [Admin.java:21](../src/main/java/com/quizapp/model/Admin.java#L21) vs [Student.java:21](../src/main/java/com/quizapp/model/Student.java#L21); [InvalidLoginException.java:11](../src/main/java/com/quizapp/exception/InvalidLoginException.java#L11) | `getDashboardTitle()`, `getHttpStatus()` |
| Dynamic method dispatch | [SceneManager.java:47](../src/main/java/com/quizapp/client/ui/SceneManager.java#L47), [ServerMain.java:140](../src/main/java/com/quizapp/server/ServerMain.java#L140) | `User` / `QuizAppException` reference, real subclass method runs |
| Object class methods | [Quiz.java:129](../src/main/java/com/quizapp/model/Quiz.java#L129) | `toString()` shown in combo boxes |
| try / multiple catch | [ApiClient.java:89](../src/main/java/com/quizapp/client/ApiClient.java#L89), [:101](../src/main/java/com/quizapp/client/ApiClient.java#L101) | `IOException` vs `InterruptedException` |
| Nested try + finally | [AttemptDAO.java:146](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L146), [:147](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L147), [:169](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L169), [:171](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L171) | transaction: commit, rollback, restore auto-commit |
| `throw` / `throws` | [AuthService.java:61](../src/main/java/com/quizapp/server/service/AuthService.java#L61), [:63](../src/main/java/com/quizapp/server/service/AuthService.java#L63) | `throws QuizAppException`, `throw new InvalidLoginException()` |
| User-defined exceptions | [QuizAppException.java:8](../src/main/java/com/quizapp/exception/QuizAppException.java#L8), [UserDAO.java:49](../src/main/java/com/quizapp/server/dao/UserDAO.java#L49) | 7 custom exceptions; duplicate username → `DuplicateUserException` |
| Exceptions → HTTP status | [QuizAppException.java:19](../src/main/java/com/quizapp/exception/QuizAppException.java#L19), [ServerMain.java:140](../src/main/java/com/quizapp/server/ServerMain.java#L140) | one handler for all, thanks to overriding |

## Unit 3 — Multithreading, I/O and Event Handling
| Topic | Where (file:line) | What to say |
|---|---|---|
| Thread via `Runnable` | [QuizTimer.java:11](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L11), [:31](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L31) | `new Thread(this, "quiz-timer")` |
| Thread via `Thread` class | [AutoSaveThread.java:24](../src/main/java/com/quizapp/client/thread/AutoSaveThread.java#L24), [:52](../src/main/java/com/quizapp/client/thread/AutoSaveThread.java#L52) | overrides `run()` |
| Life cycle (`sleep`, `wait`) | [QuizTimer.java:44](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L44), [AutoSaveThread.java:56](../src/main/java/com/quizapp/client/thread/AutoSaveThread.java#L56), [QuizTimer.java:59](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L59) | TIMED_WAITING / WAITING / stopped by `interrupt()` |
| Thread priorities | [QuizTimer.java:33](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L33), [AutoSaveThread.java:48](../src/main/java/com/quizapp/client/thread/AutoSaveThread.java#L48) | `NORM_PRIORITY`, `MIN_PRIORITY` |
| `volatile` | [QuizTimer.java:22](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L22) | flag shared between threads |
| Synchronization | [AnswerQueue.java:18](../src/main/java/com/quizapp/client/thread/AnswerQueue.java#L18), [:24](../src/main/java/com/quizapp/client/thread/AnswerQueue.java#L24), [QuizView.java:261](../src/main/java/com/quizapp/client/ui/QuizView.java#L261), [:273](../src/main/java/com/quizapp/client/ui/QuizView.java#L273) | `synchronized` + `AtomicBoolean` → submit exactly once |
| Inter-thread communication | [AnswerQueue.java:20](../src/main/java/com/quizapp/client/thread/AnswerQueue.java#L20) (`notify`), [:26](../src/main/java/com/quizapp/client/thread/AnswerQueue.java#L26) (`wait`), producer [QuizView.java:210](../src/main/java/com/quizapp/client/ui/QuizView.java#L210) | producer–consumer |
| Background → UI thread | [QuizTimer.java:42](../src/main/java/com/quizapp/client/thread/QuizTimer.java#L42), [LiveClient.java:123](../src/main/java/com/quizapp/client/LiveClient.java#L123), [Async.java:17](../src/main/java/com/quizapp/client/util/Async.java#L17) | `Platform.runLater` |
| Thread pool (extra) | [LiveHub.java:30](../src/main/java/com/quizapp/server/web/LiveHub.java#L30) | single-thread executor sends live messages |
| Byte streams (write) | [AutoSaveThread.java:97](../src/main/java/com/quizapp/client/thread/AutoSaveThread.java#L97), [ResultExporter.java:25](../src/main/java/com/quizapp/client/util/ResultExporter.java#L25) | `FileOutputStream` (+ `DataOutputStream`) |
| Byte streams (read) | [AutoSaveThread.java:113](../src/main/java/com/quizapp/client/thread/AutoSaveThread.java#L113), [ClientConfig.java:45](../src/main/java/com/quizapp/client/ClientConfig.java#L45) | `FileInputStream` (+ `DataInputStream`) |
| AWT delegation event model | [HelpWindow.java:27](../src/main/java/com/quizapp/client/awt/HelpWindow.java#L27), [:63](../src/main/java/com/quizapp/client/awt/HelpWindow.java#L63), [:105](../src/main/java/com/quizapp/client/awt/HelpWindow.java#L105) | source (Button) → `ActionEvent` → `ActionListener` |
| `KeyEvent` / `KeyListener` | [HelpWindow.java:112](../src/main/java/com/quizapp/client/awt/HelpWindow.java#L112), [:119](../src/main/java/com/quizapp/client/awt/HelpWindow.java#L119) | Esc closes (AWT) |
| Adapter class | [HelpWindow.java:67](../src/main/java/com/quizapp/client/awt/HelpWindow.java#L67) | `WindowAdapter` — override only `windowClosing` |
| JavaFX events | [LoginView.java:42](../src/main/java/com/quizapp/client/ui/LoginView.java#L42), [LoginView.java:46](../src/main/java/com/quizapp/client/ui/LoginView.java#L46), [QuizView.java:150](../src/main/java/com/quizapp/client/ui/QuizView.java#L150), [:157](../src/main/java/com/quizapp/client/ui/QuizView.java#L157), [AdminDashboard.java:100](../src/main/java/com/quizapp/client/ui/AdminDashboard.java#L100) | `setOnAction`, key filter (1–4, ←/→, F1), change listener |

## Unit 4 — JDBC and Collections
| Topic | Where (file:line) | What to say |
|---|---|---|
| Connecting to SQLite | [DatabaseManager.java:30](../src/main/java/com/quizapp/server/DatabaseManager.java#L30), [:32](../src/main/java/com/quizapp/server/DatabaseManager.java#L32) | `DriverManager.getConnection("jdbc:sqlite:…")`, foreign keys on |
| Creating the database / tables | [DatabaseManager.java:39](../src/main/java/com/quizapp/server/DatabaseManager.java#L39), `src/main/resources/schema.sql` | runs `CREATE TABLE IF NOT EXISTS` |
| Create | [UserDAO.java:28](../src/main/java/com/quizapp/server/dao/UserDAO.java#L28), [QuestionDAO.java:25](../src/main/java/com/quizapp/server/dao/QuestionDAO.java#L25) | `INSERT` with `PreparedStatement`, generated keys |
| Retrieve | [QuizDAO.java:62](../src/main/java/com/quizapp/server/dao/QuizDAO.java#L62) | `SELECT` + `ResultSet` loop |
| Update | [QuizDAO.java:72](../src/main/java/com/quizapp/server/dao/QuizDAO.java#L72) | `UPDATE … WHERE id = ?` |
| Delete | [QuizDAO.java:99](../src/main/java/com/quizapp/server/dao/QuizDAO.java#L99) | `ON DELETE CASCADE` removes questions too |
| Transaction / batch (extra) | [AttemptDAO.java:146](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L146), [:166](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L166), [:181](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L181), [:191](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L191) | commit/rollback, upsert, `addBatch` |
| List / ArrayList | [Quiz.java:21](../src/main/java/com/quizapp/model/Quiz.java#L21) | questions of a quiz |
| Map / HashMap | [QuizView.java:42](../src/main/java/com/quizapp/client/ui/QuizView.java#L42) | question id → chosen option |
| Set / HashSet | [LiveService.java:55](../src/main/java/com/quizapp/server/service/LiveService.java#L55) | keep each student's best attempt once |
| Vector | [LiveResultsView.java:57](../src/main/java/com/quizapp/client/ui/LiveResultsView.java#L57) | legacy synchronized list (activity log) |
| Hashtable vs ConcurrentHashMap | [SessionStore.java:18](../src/main/java/com/quizapp/server/SessionStore.java#L18), [LiveHub.java:28](../src/main/java/com/quizapp/server/web/LiveHub.java#L28) | thread-safe maps; comment explains why not `Hashtable` |
| Iterator | [AttemptService.java:212](../src/main/java/com/quizapp/server/service/AttemptService.java#L212) | walks the answer map |
| `Collections.shuffle` | [AttemptService.java:82](../src/main/java/com/quizapp/server/service/AttemptService.java#L82) | seed = attempt id → same order on resume |

## Unit 5 — Generics and JavaFX
| Topic | Where (file:line) | What to say |
|---|---|---|
| Generic class, two type parameters | [Pair.java:10](../src/main/java/com/quizapp/client/util/Pair.java#L10), used at [ScoreStats.java:52](../src/main/java/com/quizapp/client/util/ScoreStats.java#L52) | `Pair<K, V>` = (range name, count) |
| Generic interface | [Repository.java:13](../src/main/java/com/quizapp/server/dao/Repository.java#L13) | `Repository<T>` |
| Generic class hierarchy | [UserDAO.java:19](../src/main/java/com/quizapp/server/dao/UserDAO.java#L19), [QuizDAO.java:17](../src/main/java/com/quizapp/server/dao/QuizDAO.java#L17), [AttemptDAO.java:19](../src/main/java/com/quizapp/server/dao/AttemptDAO.java#L19) | `implements Repository<User / Quiz / Attempt>` |
| Generic methods | [ApiClient.java:38](../src/main/java/com/quizapp/client/ApiClient.java#L38), [Async.java:17](../src/main/java/com/quizapp/client/util/Async.java#L17), [LiveResultsView.java:258](../src/main/java/com/quizapp/client/ui/LiveResultsView.java#L258) | `<T> T get(...)`, `<T> void run(...)` |
| Wildcard arguments | [ScoreStats.java:31](../src/main/java/com/quizapp/client/util/ScoreStats.java#L31), [ResultExporter.java:44](../src/main/java/com/quizapp/client/util/ResultExporter.java#L44) | `List<? extends Gradable>` |
| JavaFX application skeleton | [App.java:9](../src/main/java/com/quizapp/client/App.java#L9), [:14](../src/main/java/com/quizapp/client/App.java#L14), [Launcher.java:10](../src/main/java/com/quizapp/client/Launcher.java#L10) | `extends Application`, `start(Stage)` |
| RadioButton + ToggleGroup | [QuizView.java:57](../src/main/java/com/quizapp/client/ui/QuizView.java#L57), [QuestionDialog.java:41](../src/main/java/com/quizapp/client/ui/QuestionDialog.java#L41) | one answer per question |
| ListView | [StudentDashboard.java:27](../src/main/java/com/quizapp/client/ui/StudentDashboard.java#L27) | quiz cards (custom cells) |
| ComboBox | [LiveResultsView.java:50](../src/main/java/com/quizapp/client/ui/LiveResultsView.java#L50) | pick a quiz |
| TextField | [LoginView.java:23](../src/main/java/com/quizapp/client/ui/LoginView.java#L23) | login form |
| TableView, Spinner | [AdminDashboard.java:27](../src/main/java/com/quizapp/client/ui/AdminDashboard.java#L27), [QuizEditorView.java:32](../src/main/java/com/quizapp/client/ui/QuizEditorView.java#L32) | quiz list, time limit |
| Effects | [StudentDashboard.java:118](../src/main/java/com/quizapp/client/ui/StudentDashboard.java#L118) | `DropShadow` |
| Transforms / animation | [ResultView.java:76](../src/main/java/com/quizapp/client/ui/ResultView.java#L76), [:79](../src/main/java/com/quizapp/client/ui/ResultView.java#L79), [:84](../src/main/java/com/quizapp/client/ui/ResultView.java#L84) | fade + scale together |
| MenuBar, Menu, MenuItem with image | [MainMenu.java:25](../src/main/java/com/quizapp/client/ui/MainMenu.java#L25), [:40](../src/main/java/com/quizapp/client/ui/MainMenu.java#L40), [MenuIcons.java:34](../src/main/java/com/quizapp/client/ui/MenuIcons.java#L34) | icon drawn on a Canvas → Image |
| Toolbar | [AdminDashboard.java:53](../src/main/java/com/quizapp/client/ui/AdminDashboard.java#L53) | admin actions |
| Charts (extra) | [StatsView.java:35](../src/main/java/com/quizapp/client/ui/StatsView.java#L35), [:36](../src/main/java/com/quizapp/client/ui/StatsView.java#L36) | `BarChart`, `PieChart` |

## Beyond the syllabus (worth mentioning)
| Topic | Where (file:line) |
|---|---|
| Client–server, REST + JSON | [ApiClient.java:22](../src/main/java/com/quizapp/client/ApiClient.java#L22), [ServerMain.java:100](../src/main/java/com/quizapp/server/ServerMain.java#L100) |
| WebSockets (real-time) | [LiveHub.java:41](../src/main/java/com/quizapp/server/web/LiveHub.java#L41), [:58](../src/main/java/com/quizapp/server/web/LiveHub.java#L58), [LiveClient.java:20](../src/main/java/com/quizapp/client/LiveClient.java#L20), [:59](../src/main/java/com/quizapp/client/LiveClient.java#L59), [ServerMain.java:132](../src/main/java/com/quizapp/server/ServerMain.java#L132) |
| Password hashing (PBKDF2) | [PasswordUtil.java:16](../src/main/java/com/quizapp/server/PasswordUtil.java#L16), [:50](../src/main/java/com/quizapp/server/PasswordUtil.java#L50) |
| Login check / admin-only routes | [Auth.java:25](../src/main/java/com/quizapp/server/web/Auth.java#L25), [:48](../src/main/java/com/quizapp/server/web/Auth.java#L48) |
| No answers sent to students | [QuestionView.java:6](../src/main/java/com/quizapp/dto/QuestionView.java#L6) |
| Automated tests (44) | `src/test/java/…` — run `.\mvnw.cmd test` |
