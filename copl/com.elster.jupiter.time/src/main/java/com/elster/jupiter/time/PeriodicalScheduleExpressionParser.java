package com.elster.jupiter.time;

import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.util.Optional;
import java.util.regex.Pattern;

public enum PeriodicalScheduleExpressionParser implements ScheduleExpressionParser {

    INSTANCE;

    private static final Pattern PATTERN = Pattern.compile("P\\[(\\d+),(MINUTE|HOUR|DAY|WEEK|MONTH|YEAR),.*\\]");

    private static final int COUNT_GROUP = 1;
    private static final int EVERY_GROUP = 1;

    @Override
    public Optional<PeriodicalScheduleExpression> parse(String string) {
        return Optional.empty();
    }

}
