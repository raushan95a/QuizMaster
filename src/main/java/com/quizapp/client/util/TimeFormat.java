package com.quizapp.client.util;

/** Formats seconds as m:ss (e.g. 125 -> "2:05"). */
public final class TimeFormat {

    private TimeFormat() {
    }

    public static String minutesSeconds(int totalSeconds) {
        int s = Math.max(0, totalSeconds);
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
