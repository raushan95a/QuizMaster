package com.quizapp.server.web;

import com.quizapp.dto.LoginRequest;
import com.quizapp.dto.RegisterRequest;
import com.quizapp.dto.UserDto;
import com.quizapp.model.User;
import com.quizapp.server.service.AuthService;
import io.javalin.http.Context;

/** HTTP endpoints under /api/auth. Only reads the request and calls AuthService. */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void register(Context ctx) throws Exception {
        ctx.status(201).json(authService.register(RequestUtil.body(ctx, RegisterRequest.class)));
    }

    public void login(Context ctx) throws Exception {
        ctx.json(authService.login(RequestUtil.body(ctx, LoginRequest.class)));
    }

    public void logout(Context ctx) {
        authService.logout(ctx.attribute(Auth.TOKEN));
        ctx.status(204);
    }

    public void me(Context ctx) {
        User user = Auth.currentUser(ctx);
        ctx.json(UserDto.from(user));
    }
}
