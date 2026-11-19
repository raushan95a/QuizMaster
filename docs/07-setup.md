# 07 — Run Locally

## Requirements
| Tool | Notes |
|---|---|
| JDK 17 or newer | `java -version` |
| Git | to push to GitHub |
| IDE (optional) | IntelliJ IDEA or VS Code — open the folder as a Maven project |

Maven is **not** needed (use `mvnw.cmd`, it downloads Maven itself). JavaFX, Javalin, Jackson and the SQLite driver
are downloaded automatically as dependencies. Docker is **not** needed on your PC.

## Run (PowerShell, in the project folder)
```
# 1. Build (also runs the tests)
.\mvnw.cmd clean package

# 2. Terminal 1 — start the server (http://localhost:7070)
java -jar target\quizmaster-server.jar

# 3. Terminal 2 — start the app
.\mvnw.cmd javafx:run

# Extra app windows (to act as more students): run step 3 again in more terminals
```
Login as `admin` / `admin123`, or register a student.

Use the hosted server instead of localhost:
```
.\mvnw.cmd javafx:run "-Dserver.url=https://your-app.up.railway.app"
```

## Server settings (environment variables)
| Variable | Default | Meaning |
|---|---|---|
| `APP_ENV` | `dev` | `dev` seeds admin/admin123; `prod` requires `ADMIN_USERNAME`/`ADMIN_PASSWORD` |
| `PORT` | `7070` | port |
| `DB_PATH` | `data/quiz.db` | database file |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | — | first admin in production |

## Reset data
Stop the server and delete the `data` folder.

## Troubleshooting
| Problem | Fix |
|---|---|
| `'mvnw.cmd' is not recognized` | type `.\mvnw.cmd` |
| `JAVA_HOME` error | set `JAVA_HOME` to your JDK folder, e.g. `C:\Program Files\Java\jdk-26` |
| App says "Cannot reach server" | start the server first / check `server.url` |
| `Address already in use` | another server is running on 7070 — close it |
