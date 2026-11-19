package com.quizapp.server.web;

import com.quizapp.exception.ForbiddenException;
import com.quizapp.exception.UnauthorizedException;
import com.quizapp.model.Role;
import com.quizapp.model.User;
import com.quizapp.server.service.AuthService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Set;

/** Checks the "Authorization: Bearer <token>" header before every protected /api request. */
public final class Auth {

    static final String USER = "user";
    static final String TOKEN = "token";

    /** Paths anyone may call without logging in. */
    private static final Set<String> PUBLIC_PREFIXES = Set.of("/api/auth/login", "/api/auth/register", "/api/live");

    private Auth() {
    }

    /** Before-handler: finds the user for the token and stores it on the request. */
    public static Handler filter(AuthService authService) {
        return ctx -> {
            String path = ctx.path();
            for (String prefix : PUBLIC_PREFIXES) {
                if (path.startsWith(prefix)) {
                    return;
                }
            }
            String header = ctx.header("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new UnauthorizedException();
            }
            String token = header.substring("Bearer ".length()).trim();
            ctx.attribute(USER, authService.userForToken(token));
            ctx.attribute(TOKEN, token);
        };
    }

    public static User currentUser(Context ctx) {
        return ctx.attribute(USER);
    }

    /** Throws ForbiddenException unless the current user is an admin. */
    public static User requireAdmin(Context ctx) throws ForbiddenException {
        User user = currentUser(ctx);
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new ForbiddenException();
        }
        return user;
    }
}
