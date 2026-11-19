package com.quizapp.model;

/**
 * Base class for every user. Abstract: a plain "User" never exists, only an Admin or a Student.
 */
public abstract class User {

    // protected: visible to the subclasses Admin and Student (member access in inheritance)
    protected int id;
    protected String username;
    protected String fullName;
    protected String passwordHash;
    protected String salt;

    protected User(int id, String username, String fullName, String passwordHash, String salt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    /** Each subclass says which role it is. */
    public abstract Role getRole();

    /** Title shown at the top of the dashboard; overridden by each subclass. */
    public abstract String getDashboardTitle();

    /**
     * Factory method: builds the right subclass for a role.
     * The caller gets a User reference, but the real object is an Admin or a Student.
     */
    public static User create(Role role, int id, String username, String fullName,
                              String passwordHash, String salt) {
        return switch (role) {
            case ADMIN -> new Admin(id, username, fullName, passwordHash, salt);
            case STUDENT -> new Student(id, username, fullName, passwordHash, salt);
        };
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ", " + getRole() + ")";
    }
}
