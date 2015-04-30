package com.elster.jupiter.devtools.tests.assertions;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Failures;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogRecorderAssert extends AbstractAssert<LogRecorderAssert, LogRecorder> {

    private final Failures failures = Failures.instance();

    private LogRecord lookingAt;

    LogRecorderAssert(LogRecorder actual) {
        super(actual, LogRecorderAssert.class);
    }

    public LogRecorderAssert hasRecordWithMessage(Predicate<String> messagePredicate) {
        lookingAt = actual.getRecords().stream()
                .filter(logRecord -> messagePredicate.test(logRecord.getMessage()))
                .findFirst()
                .orElse(null);

        if (lookingAt == null) {
            throw failures.failure("No log record with message matching the predicate.");
        }

        return myself;
    }

    public LogRecorderAssert hasRecordWithMessage(String message) {
        lookingAt = actual.getRecords().stream()
                .filter(logRecord -> Objects.equals(message, logRecord.getMessage()))
                .findFirst()
                .orElse(null);

        if (lookingAt == null) {
            throw failures.failure("No log record with message " + message);
        }

        return myself;
    }

    public LogRecorderAssert atLevel(Level level) {
        if (!lookingAt.getLevel().equals(level)) {
            throw failures.failure("Log record did not have the expected level : " + level.toString());
        }
        return myself;
    }
}
