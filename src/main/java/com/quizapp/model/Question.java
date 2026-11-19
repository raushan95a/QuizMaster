package com.quizapp.model;

/**
 * One multiple-choice question with four options (A-D) and one correct option.
 * Plain Java bean (no-arg constructor + getters/setters) so Jackson can turn it into JSON and back.
 */
public class Question {

    /** The four option letters, in order. */
    public static final char[] OPTIONS = {'A', 'B', 'C', 'D'};

    private int id;
    private int quizId;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // "A", "B", "C" or "D"

    // ---- constructor overloading ----

    public Question() {
    }

    /** A new question (not saved yet, so no id). */
    public Question(String text, String optionA, String optionB, String optionC, String optionD,
                    String correctOption) {
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    /** A question loaded from the database. */
    public Question(int id, int quizId, String text, String optionA, String optionB, String optionC,
                    String optionD, String correctOption) {
        this(text, optionA, optionB, optionC, optionD, correctOption);
        this.id = id;
        this.quizId = quizId;
    }

    /** Text of the option for a letter (A-D), or null for an unknown letter. */
    public String optionText(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'A':
                return optionA;
            case 'B':
                return optionB;
            case 'C':
                return optionC;
            case 'D':
                return optionD;
            default:
                return null;
        }
    }

    public boolean isCorrect(String chosen) {
        return chosen != null && chosen.equalsIgnoreCase(correctOption);
    }

    // ---- getters / setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    @Override
    public String toString() {
        return text;
    }
}
