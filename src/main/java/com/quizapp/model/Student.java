package com.quizapp.model;

/** A student who takes quizzes. */
public class Student extends User {

    public Student(int id, String username, String fullName, String passwordHash, String salt) {
        super(id, username, fullName, passwordHash, salt);
    }

    /** Used on the client, where password data is never present. */
    public Student(int id, String username, String fullName) {
        this(id, username, fullName, null, null);
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    @Override
    public String getDashboardTitle() {
        return "Welcome, " + fullName;
    }
}
