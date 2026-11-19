package com.quizapp.model;

/** A teacher who creates quizzes and watches results. */
public class Admin extends User {

    public Admin(int id, String username, String fullName, String passwordHash, String salt) {
        super(id, username, fullName, passwordHash, salt);
    }

    /** Used on the client, where password data is never present. */
    public Admin(int id, String username, String fullName) {
        this(id, username, fullName, null, null);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard - " + fullName;
    }
}
