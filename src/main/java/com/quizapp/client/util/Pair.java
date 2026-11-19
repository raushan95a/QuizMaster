package com.quizapp.client.util;

/**
 * A generic class with TWO type parameters: holds two related values,
 * e.g. Pair<String, Integer> = ("40-60 %", 7) for one bar of a chart.
 *
 * @param <K> type of the first value
 * @param <V> type of the second value
 */
public class Pair<K, V> {

    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
