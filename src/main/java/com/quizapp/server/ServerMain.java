package com.quizapp.server;

import com.quizapp.dto.ErrorResponse;
import com.quizapp.exception.QuizAppException;
import com.quizapp.server.dao.UserDAO;
import com.quizapp.server.service.AuthService;
import com.quizapp.server.web.Auth;
import com.quizapp.server.web.AuthController;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.http.staticfiles.Location;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Entry point of the server program: java -jar quizmaster-server.jar */
public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) throws Exception {
        Config config = Config.fromEnvironment();
        createApp(config).start(config.getPort());
        LOG.info("QuizMaster server running on port {} ({} mode)", config.getPort(), config.isDev() ? "dev" : "prod");
    }

    /** Builds the whole server (separate from main so tests can start it too). */
    public static Javalin createApp(Config config) throws Exception {
        // Database
        DatabaseManager db = new DatabaseManager(config.getDbPath());
        db.createTables();

        // DAOs -> services -> controllers
        UserDAO userDAO = new UserDAO(db);
        AuthService authService = new AuthService(userDAO, new SessionStore());
        if (authService.ensureAdminExists(config.getAdminUsername(), config.getAdminPassword())) {
            LOG.info("Created admin account '{}'", config.getAdminUsername());
        }
        AuthController authController = new AuthController(authService);

        Javalin app = Javalin.create(javalin -> {
            javalin.staticFiles.add("/public", Location.CLASSPATH); // index.html, live.html
            javalin.showJavalinBanner = false;
        });

        // Every /api request (except public ones) must carry a valid token
        app.before("/api/*", Auth.filter(authService));

        // Routes
        app.get("/health", ctx -> ctx.json(Map.of("status", "UP")));
        app.post("/api/auth/register", authController::register);
        app.post("/api/auth/login", authController::login);
        app.post("/api/auth/logout", authController::logout);
        app.get("/api/auth/me", authController::me);

        // Errors -> {"error": "..."} with the right status code.
        // e.getHttpStatus() is resolved at runtime by the real exception class (dynamic method dispatch).
        app.exception(QuizAppException.class, (e, ctx) -> {
            if (e.getHttpStatus() >= 500) {
                LOG.error("Server error on {} {}", ctx.method(), ctx.path(), e);
            }
            ctx.status(e.getHttpStatus()).json(new ErrorResponse(e.getMessage()));
        });
        app.exception(HttpResponseException.class, (e, ctx) ->
                ctx.status(e.getStatus()).json(new ErrorResponse(e.getMessage())));
        app.exception(Exception.class, (e, ctx) -> {
            LOG.error("Unexpected error on {} {}", ctx.method(), ctx.path(), e);
            ctx.status(500).json(new ErrorResponse("Something went wrong on the server"));
        });

        return app;
    }
}
