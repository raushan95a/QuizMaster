package com.quizapp.client.util;

import com.quizapp.dto.ResultDto;
import com.quizapp.model.Gradable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Saves quiz results as a CSV file (opens in Excel), written with a byte stream (FileOutputStream).
 */
public final class ResultExporter {

    /** Byte Order Mark: tells Excel the file is UTF-8, so names with accents show correctly. */
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private ResultExporter() {
    }

    /** Writes one row per result plus an average line. Returns the number of rows written. */
    public static int export(String quizTitle, List<ResultDto> rows, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(UTF8_BOM);
            writeLine(out, "Quiz", quizTitle);
            writeLine(out, "Student", "Score", "Total", "Percent", "Correct", "Wrong", "Unanswered",
                    "Time (sec)", "Submitted at", "Auto-submitted");
            for (ResultDto r : rows) {
                writeLine(out, r.studentName(), String.valueOf(r.score()), String.valueOf(r.total()),
                        String.valueOf(r.percentage()), String.valueOf(r.correct()), String.valueOf(r.wrong()),
                        String.valueOf(r.unanswered()), String.valueOf(r.timeTakenSec()), r.submittedAt(),
                        r.autoSubmitted() ? "yes" : "no");
            }
            writeLine(out, "Average percent", String.format("%.1f", averagePercentage(rows)));
        }
        return rows.size();
    }

    /**
     * Wildcard generic: works for a list of ANY Gradable type (List<ResultDto>, List<Attempt>, ...).
     */
    public static double averagePercentage(List<? extends Gradable> items) {
        if (items.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Gradable g : items) {
            sum += g.getPercentage();
        }
        return sum / items.size();
    }

    private static void writeLine(OutputStream out, String... values) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(values[i]));
        }
        line.append("\r\n");
        out.write(line.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Quotes a value if it contains a comma, quote or line break (CSV rules). */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        // A leading = + - @ would make Excel treat the cell as a formula; prefix it to keep it as text
        String safe = value.matches("^[=+\\-@].*") && !value.matches("^-?[0-9.]+$") ? "'" + value : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
