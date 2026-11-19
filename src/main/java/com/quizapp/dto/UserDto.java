package com.quizapp.dto;

import com.quizapp.model.Admin;
import com.quizapp.model.Role;
import com.quizapp.model.Student;
import com.quizapp.model.User;

/** User data that is safe to send over the network (no password hash or salt). */
public record UserDto(int id, String username, String fullName, Role role) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }

    /** Turns the DTO back into a real Admin or Student object (used by the client). */
    public User toUser() {
        return role == Role.ADMIN
                ? new Admin(id, username, fullName)
                : new Student(id, username, fullName);
    }
}
