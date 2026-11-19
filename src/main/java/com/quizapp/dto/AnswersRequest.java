package com.quizapp.dto;

import java.util.Map;

/** Body of progress / submit: question id -> chosen option ("A".."D"). */
public record AnswersRequest(Map<Integer, String> answers, boolean autoSubmitted) {
}
