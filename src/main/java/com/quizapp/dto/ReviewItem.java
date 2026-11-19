package com.quizapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** One question after submitting: what the student chose and what was correct. */
public record ReviewItem(String text, String optionA, String optionB, String optionC, String optionD,
                         String chosen, String correct) {

    public String option(char letter) {
        switch (letter) {
            case 'A':
                return optionA;
            case 'B':
                return optionB;
            case 'C':
                return optionC;
            default:
                return optionD;
        }
    }

    @JsonIgnore // helper, not part of the JSON
    public boolean isRight() {
        return chosen != null && chosen.equals(correct);
    }
}
