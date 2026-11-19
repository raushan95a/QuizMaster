# 09 — Deployment (putting the server online)

Only the **server** goes online. The JavaFX app runs on each laptop and connects to the server's URL.

## Do I need Docker?
**No — you don't install or learn Docker.** The repo has a small `Dockerfile` (a recipe that says "build the jar
with Maven, then run it with Java"). The hosting website reads that recipe and does everything itself.

## Where to host
SQLite is a file, so the host must offer a **persistent volume** (disk that survives restarts).
**Railway** is recommended: deploy from GitHub in a few clicks, gives an HTTPS URL, supports volumes and WebSockets.
(Free hosts without a persistent disk — e.g. Render's free tier — would erase the database on every restart.)
Pricing and free trials change — check railway.com before signing up.

## Steps (Railway)
1. Push the code to GitHub (`raushan95a/QuizMaster`).
2. railway.com → sign in with GitHub → **New Project → Deploy from GitHub repo** → choose `QuizMaster`.
   Railway finds the `Dockerfile` and builds automatically (first build takes a few minutes).
3. In the service: **Volumes → Add volume**, mount path `/data`.
4. **Variables** → add:
   | Name | Value |
   |---|---|
   | `APP_ENV` | `prod` |
   | `DB_PATH` | `/data/quiz.db` |
   | `ADMIN_USERNAME` | your admin username |
   | `ADMIN_PASSWORD` | a strong password (keep it secret, never commit it) |
5. **Settings → Networking → Generate Domain** → you get `https://<name>.up.railway.app`.
6. Open `https://<name>.up.railway.app/health` → should show `{"status":"UP"}`.
7. On your laptop: `.\mvnw.cmd javafx:run "-Dserver.url=https://<name>.up.railway.app"` and log in as admin.

Every `git push` to `main` redeploys automatically. The database in `/data` is kept.

## Before the demo
- Open `/health` 10 minutes early.
- Download a copy of the database (Railway volume → backup, or ask Claude to add an admin "Download backup" button).

## No internet at the venue (fallback)
Run `java -jar target\quizmaster-server.jar` on one laptop, connect all laptops to a phone hotspot, and start the
apps with `-Dserver.url=http://<that-laptop-ip>:7070`.
