package com.quizapp.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.quizapp.model.Gradable;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreStatsTest {

    /** Minimal Gradable for tests. */
    private record Score(int getScore, int getTotal) implements Gradable {
    }

    @Test
    void computesAverageHighLowAndPassFail() {
        ScoreStats s = ScoreStats.of(List.of(new Score(10, 10), new Score(5, 10), new Score(1, 10)));
        assertEquals(3, s.getCount());
        assertEquals(53.3, s.getAverage());
        assertEquals(100.0, s.getHighest());
        assertEquals(10.0, s.getLowest());
        assertEquals(2, s.getPassed());   // 100 % and 50 %
        assertEquals(1, s.getFailed());
    }

    @Test
    void putsScoresInTheRightRanges() {
        ScoreStats s = ScoreStats.of(List.of(
                new Score(0, 10), new Score(1, 10),   // 0-20
                new Score(5, 10),                      // 40-60
                new Score(10, 10)));                   // 100 -> last range
        List<Pair<String, Integer>> r = s.getRanges();
        assertEquals(5, r.size());
        assertEquals("0-20 %", r.get(0).getFirst());
        assertEquals(2, r.get(0).getSecond());
        assertEquals(0, r.get(1).getSecond());
        assertEquals(1, r.get(2).getSecond());
        assertEquals(0, r.get(3).getSecond());
        assertEquals(1, r.get(4).getSecond());
    }

    @Test
    void emptyListGivesZeros() {
        ScoreStats s = ScoreStats.of(List.of());
        assertEquals(0, s.getCount());
        assertEquals(0.0, s.getAverage());
        assertEquals(0.0, s.getLowest());
    }
}
