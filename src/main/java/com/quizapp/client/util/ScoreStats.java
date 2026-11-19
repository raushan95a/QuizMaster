package com.quizapp.client.util;

import com.quizapp.model.Gradable;
import java.util.ArrayList;
import java.util.List;

/** Statistics for a list of results: average, highest, lowest, pass/fail and score ranges. */
public class ScoreStats {

    public static final double PASS_PERCENT = 50.0;
    private static final String[] RANGE_NAMES = {"0-20 %", "20-40 %", "40-60 %", "60-80 %", "80-100 %"};

    private final int count;
    private final double average;
    private final double highest;
    private final double lowest;
    private final int passed;
    private final List<Pair<String, Integer>> ranges;

    private ScoreStats(int count, double average, double highest, double lowest, int passed,
                       List<Pair<String, Integer>> ranges) {
        this.count = count;
        this.average = average;
        this.highest = highest;
        this.lowest = lowest;
        this.passed = passed;
        this.ranges = ranges;
    }

    /** Works for any list of Gradable objects (wildcard: ResultDto, Attempt, ...). */
    public static ScoreStats of(List<? extends Gradable> results) {
        int[] buckets = new int[RANGE_NAMES.length];
        double sum = 0;
        double high = 0;
        double low = results.isEmpty() ? 0 : 100;
        int passed = 0;

        for (Gradable g : results) {
            double p = g.getPercentage();
            sum += p;
            high = Math.max(high, p);
            low = Math.min(low, p);
            if (p >= PASS_PERCENT) {
                passed++;
            }
            int index = (int) (p / 20);           // 0..20 -> 0, 20..40 -> 1, ... 100 -> 5
            buckets[Math.min(index, buckets.length - 1)]++;
        }

        List<Pair<String, Integer>> ranges = new ArrayList<>();
        for (int i = 0; i < buckets.length; i++) {
            ranges.add(new Pair<>(RANGE_NAMES[i], buckets[i]));
        }
        double average = results.isEmpty() ? 0 : Math.round(sum / results.size() * 10) / 10.0;
        return new ScoreStats(results.size(), average, high, low, passed, ranges);
    }

    public int getCount() {
        return count;
    }

    public double getAverage() {
        return average;
    }

    public double getHighest() {
        return highest;
    }

    public double getLowest() {
        return lowest;
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return count - passed;
    }

    public List<Pair<String, Integer>> getRanges() {
        return ranges;
    }
}
