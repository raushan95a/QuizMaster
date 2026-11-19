# 05 — Syllabus Mapping

For the viva. Replace class names with exact `File.java:line` references as code is finished.

## Unit 1 — Java basics and inheritance
| Topic | Where |
|---|---|
| Data types, arrays, operators, control statements | Grading loop in `AttemptService.finish()`; `Question.OPTIONS` (`char[]`); `switch` in `Question.optionText()`; `RadioButton[]` in `QuizView` |
| Classes, objects, constructors, methods, `this` | `model` classes |
| `static` | `PasswordUtil`, `Config.load()`, `AlertUtil`, `Session` |
| `final` | Constants (`DEFAULT_PORT`, `GRACE_SECONDS`), `final class PasswordUtil` |
| Inheritance, member access | `Admin` / `Student` extend `User` (`protected` fields) |
| Abstract class | `User` |
| Interface | `Repository<T>`; `Gradable` (with a `default getPercentage()` method), implemented by `Attempt` and `ResultDto` |
| Multilevel hierarchy, `super` | `Exception` → `QuizAppException` → `InvalidLoginException`; `super(message)` |
| Packages, access specifiers | `com.quizapp.model / server / client …`; private fields + public getters |

## Unit 2 — Polymorphism and exceptions
| Topic | Where |
|---|---|
| Method overloading | `ApiClient.get(path, Class<T>)` / `get(path, TypeReference<T>)`; `AlertUtil.error(String)` / `error(String, Throwable)` |
| Constructor overloading | `Question` and `Quiz` constructors with / without id |
| Method overriding | `getRole()`, `getDashboardTitle()`, `getHttpStatus()`, `toString()` |
| Dynamic method dispatch | Error handler calls `e.getHttpStatus()` on a `QuizAppException` reference; `user.getDashboardTitle()` |
| try / catch, multiple catch | `ApiClient` catches `IOException` and `InterruptedException` separately |
| Nested try, finally | `AttemptDAO.submit()` transaction (inner try + rollback, `finally` restores auto-commit) |
| throw / throws | Services throw `ValidationException`, `InvalidLoginException`; methods declare `throws QuizAppException` |
| User-defined exceptions | `exception` package (7 classes) |
| Object class | `toString()` used by `ListView`/`ComboBox`; `Object` lock in `AnswerQueue` |

## Unit 3 — Multithreading, I/O, events
| Topic | Where |
|---|---|
| Thread via `Runnable` | `QuizTimer implements Runnable` |
| Thread via `Thread` class | `AutoSaveThread extends Thread` |
| Life cycle, priorities | Timer sleeps 1 s per tick; `autoSave.setPriority(Thread.MIN_PRIORITY)` |
| Synchronization | `synchronized submit()`; `synchronized put/take` in `AnswerQueue` |
| Inter-thread communication | `wait()` / `notify()` in `AnswerQueue` (producer–consumer); `Platform.runLater` |
| Byte streams, `FileOutputStream`, `FileInputStream` | `AutoSaveThread` backup file (`DataOutputStream`); reading it back (`DataInputStream`); `ResultExporter` CSV |
| Delegation event model, listeners | AWT `HelpWindow`: `addActionListener`, `KeyListener`; JavaFX `setOnAction` |
| `KeyEvent` | Esc in `HelpWindow`; keys 1–4, ←/→ in `QuizView`; Enter in `LoginView` |
| Adapter classes | `WindowAdapter` in `HelpWindow` |

## Unit 4 — JDBC and collections
| Topic | Where |
|---|---|
| JDBC with SQLite, creating DB and tables | `DatabaseManager` (`DriverManager`, runs `schema.sql`) |
| Create / Retrieve / Update / Delete | `UserDAO`, `QuizDAO`, `QuestionDAO`, `AttemptDAO` (`PreparedStatement`) |
| ArrayList, LinkedList | `List<Question>`; `LinkedList` screen history in `SceneManager` |
| HashSet, TreeSet | answered question ids; sorted subjects |
| HashMap, Hashtable | answers `HashMap<Integer, Character>`; `SessionStore` (`ConcurrentHashMap`, compared with `Hashtable`) |
| Vector | activity list in `LiveResultsView` (legacy synchronized list) |
| ConcurrentHashMap / Set | `LiveHub` subscribers per quiz (thread-safe, used by WebSocket threads) |
| Iterator | `AttemptService.cleanAnswers()` walks the answer map with an `Iterator` |
| `Collections.shuffle` | `AttemptService.start()` — `shuffle(list, new Random(attemptId))`: new order per attempt, same order on resume |

## Unit 5 — Generics and JavaFX
| Topic | Where |
|---|---|
| Generic class / interface / hierarchy | `Repository<T>` implemented by DAOs; `Pair<K, V>` |
| Generic method | `ApiClient.<T>post(path, body, Class<T>)`; `Async.run(Callable<T>, …)` |
| Wildcards | `ResultExporter.export(List<? extends Gradable>)` |
| JavaFX skeleton | `App extends Application`, `start(Stage)` |
| Controls | `RadioButton`, `ListView`, `ComboBox`, `TextField`, `TableView`, `Spinner` |
| Effects & transforms | `DropShadow` on quiz cards; `FadeTransition` + `ScaleTransition` on the result |
| Menus, toolbar | `MenuBar` with icon `MenuItem`s; admin `ToolBar` |

## Beyond the syllabus (worth mentioning)
| Topic | Where |
|---|---|
| Client–server architecture | JavaFX app ⇄ Javalin server ⇄ SQLite |
| REST + JSON (Jackson) | `server/web` controllers, `dto` records, `ApiClient` |
| WebSockets (real-time) | `LiveHub` (`/ws/live`), `live.html`, admin `LiveResultsView` |
| `java.net.http.HttpClient` | `ApiClient` (HTTP) and `LiveClient` (WebSocket) |
| Password hashing (PBKDF2) | `PasswordUtil` — `PBKDF2WithHmacSHA256`, random salt |
| Docker deployment | `Dockerfile`, hosted server with persistent disk |
