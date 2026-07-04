package com.martialarts.backend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    // strict format (BEST PRACTICE)
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-M-d");

    private static final DateTimeFormatter STRICT_OUTPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // -------- SAFE PARSER --------
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format. Expected yyyy-M-d but got: " + dateStr);
        }
    }

    // -------- SAFE FORMATTER --------
    public static String formatDate(LocalDate date) {
        return date.format(STRICT_OUTPUT);
    }

    // -------- LocalDateTime safe parse (if needed) --------
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid DateTime format: " + dateTimeStr);
        }
    }
}