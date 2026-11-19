package com.quizapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A quiz: title, subject, time limit and its questions.
 * Plain Java bean so Jackson can turn it into JSON and back.
 */
public class Quiz {

    private int id;
    private String title;
    private String subject;
    private String description;
    private int timeLimitMin;
    private boolean published;
    private int createdBy;
    private String createdAt;
    private int questionCount;
    private List<Question> questions = new ArrayList<>();

    // ---- constructor overloading ----

    public Quiz() {
    }

    /** A new quiz (not saved yet). */
    public Quiz(String title, String subject, String description, int timeLimitMin) {
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.timeLimitMin = timeLimitMin;
    }

    /** A quiz loaded from the database. */
    public Quiz(int id, String title, String subject, String description, int timeLimitMin,
                boolean published, int createdBy, String createdAt) {
        this(title, subject, description, timeLimitMin);
        this.id = id;
        this.published = published;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // ---- getters / setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTimeLimitMin() {
        return timeLimitMin;
    }

    public void setTimeLimitMin(int timeLimitMin) {
        this.timeLimitMin = timeLimitMin;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions == null ? new ArrayList<>() : questions;
    }

    @Override
    public String toString() {
        return title + " (" + subject + ")";
    }
}
