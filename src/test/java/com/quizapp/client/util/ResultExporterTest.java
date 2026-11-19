package com.quizapp.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quizapp.dto.ResultDto;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResultExporterTest {

    @TempDir
    Path tempDir;

    private ResultDto row(String name, int score) {
        return new ResultDto(1, 1, "Quiz", name, score, 4, score * 25.0, score, 4 - score, 0, 90, false,
                "2026-11-19T10:00:00Z");
    }

    @Test
    void writesHeaderRowsAndAverage() throws Exception {
        File file = tempDir.resolve("results.csv").toFile();
        int count = ResultExporter.export("Unit 1", List.of(row("Ravi", 4), row("Anu", 2)), file);

        assertEquals(2, count);
        String csv = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("﻿Quiz,Unit 1"));      // UTF-8 BOM for Excel
        assertTrue(csv.contains("\r\nRavi,4,4,100.0,"));
        assertTrue(csv.contains("\r\nAnu,2,4,50.0,"));
        assertTrue(csv.contains("Average percent,75.0"));
    }

    @Test
    void escapesSpecialCharacters() {
        assertEquals("plain", ResultExporter.escape("plain"));
        assertEquals("\"Kumar, Ravi\"", ResultExporter.escape("Kumar, Ravi"));
        assertEquals("\"say \"\"hi\"\"\"", ResultExporter.escape("say \"hi\""));
        assertEquals("'=1+1", ResultExporter.escape("=1+1"));   // not run as a formula in Excel
        assertEquals("-5", ResultExporter.escape("-5"));        // numbers stay numbers
        assertEquals("", ResultExporter.escape(null));
    }

    @Test
    void averageWorksForAnyGradableList() {
        assertEquals(0.0, ResultExporter.averagePercentage(List.of()));
        assertEquals(62.5, ResultExporter.averagePercentage(List.of(row("A", 4), row("B", 1))));
    }
}
