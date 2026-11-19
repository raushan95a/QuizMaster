package com.quizapp.dto;

import com.quizapp.model.Question;

/** A question as the student sees it while writing: NO correct option. */
public record QuestionView(int id, String text, String optionA, String optionB, String optionC, String optionD) {

    public static QuestionView from(Question q) {
        return new QuestionView(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(),
                q.getOptionD());
    }

    /** Text of option A-D. */
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
}
