package com.happyhome.rental.email.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RentalNoticeDateParser {

    private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2})\\D?(\\d{1,2})\\D?(\\d{1,2})");

    private RentalNoticeDateParser() {
    }

    public static Optional<LocalDate> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = DATE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            ));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }
}
