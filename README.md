# QuizMaster Lite — Online Quiz / Exam System

A JavaFX quiz app connected to a Java server hosted online (Javalin + JDBC + SQLite), with a live results page
that faculty can open on any phone. Course project for **Advanced Java Programming (CSE 307/309), SRM University AP**.

```
JavaFX app (admin / students) ──HTTP──► Java server (online) ──JDBC──► SQLite
Faculty phone ──► https://<server>/live.html?quiz=<id>   (instant updates via WebSocket)
```

## Features
- Login / register with roles (Admin, Student)
- Admin: create quizzes and questions, publish, watch results live, export CSV
- Student: timed quiz (auto-submit at zero), background auto-save, instant result and answer review
- Live leaderboard page for the faculty

## Run locally
```
.\mvnw.cmd clean package
java -jar target\quizmaster-server.jar
.\mvnw.cmd javafx:run
```
Dev admin login: `admin` / `admin123`. More in [docs/07-setup.md](docs/07-setup.md).

## Docs
[Overview](docs/01-overview.md) · [Architecture](docs/02-architecture.md) · [Database](docs/03-database.md) ·
[Screens](docs/04-ui-screens.md) · [Syllabus mapping](docs/05-syllabus-mapping.md) · [Roadmap](docs/06-roadmap.md) ·
[Setup](docs/07-setup.md) · [API](docs/08-api.md) · [Deployment](docs/09-deployment.md) ·
[Viva guide](docs/10-viva-guide.md)
