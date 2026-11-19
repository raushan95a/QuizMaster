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

## Live demo over Wi-Fi (no internet needed)
1. Connect your laptop and every demo device (classmates' laptops, faculty phone) to the **same Wi-Fi or phone hotspot**.
2. Start the server on your laptop. It prints the addresses to use, for example:
   ```
   On this network: app -Dserver.url=http://10.7.137.162:7070   live page: http://10.7.137.162:7070/live.html
   ```
3. Faculty open the **live page** link on their phone (pick the quiz from the list), or use
   Admin → **● Live Results** → **Copy link** / **Open in browser**.
4. Classmates run the app pointed at your laptop:
   ```
   .\mvnw.cmd javafx:run "-Dserver.url=http://10.7.137.162:7070"
   ```
5. If phones cannot connect, allow Java through Windows Firewall (Private networks) when Windows asks,
   or check that all devices are on the same network.

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
