/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.ZonedDateTime;
import java.util.Optional;

public enum Never implements ScheduleExpression, ScheduleExpressionParser {
    NEVER;

    @Override
    public Optional<ZonedDateTime> nextOccurrence(ZonedDateTime time) {
        return Optional.empty();
    }

    @Override
    public String encoded() {
        return "NEVER";
    }

    @Override
    public Optional<? extends ScheduleExpression> parse(String string) {
        return encoded().equals(string) ? Optional.of(this) : Optional.<ScheduleExpression>empty();
    }
}
