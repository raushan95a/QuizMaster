package com.quizapp.model;

/** One student's attempt at one quiz. IN_PROGRESS while writing, SUBMITTED after grading. */
public class Attempt implements Gradable {

    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String SUBMITTED = "SUBMITTED";

    private int id;
    private int userId;
    private int quizId;
    private String quizTitle;
    private String status;
    private String startedAt;
    private String submittedAt;
    private int score;
    private int total;
    private int correctCount;
    private int wrongCount;
    private int unansweredCount;
    private int timeTakenSec;
    private boolean autoSubmitted;

    public Attempt() {
    }

    /** A new attempt that has just started. */
    public Attempt(int userId, int quizId, String startedAt) {
        this.userId = userId;
        this.quizId = quizId;
        this.startedAt = startedAt;
        this.status = IN_PROGRESS;
    }

    public boolean isSubmitted() {
        return SUBMITTED.equals(status);
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getTotal() {
        return total;
    }

    // ---- getters / setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public int getUnansweredCount() {
        return unansweredCount;
    }

    public void setUnansweredCount(int unansweredCount) {
        this.unansweredCount = unansweredCount;
    }

    public int getTimeTakenSec() {
        return timeTakenSec;
    }

    public void setTimeTakenSec(int timeTakenSec) {
        this.timeTakenSec = timeTakenSec;
    }

    public boolean isAutoSubmitted() {
        return autoSubmitted;
    }

    public void setAutoSubmitted(boolean autoSubmitted) {
        this.autoSubmitted = autoSubmitted;
    }
}
