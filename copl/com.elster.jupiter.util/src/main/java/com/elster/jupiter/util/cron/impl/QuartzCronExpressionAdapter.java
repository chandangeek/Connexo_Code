/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Adapter around a QuartzCronExpression to implement CronExpression
 */
class QuartzCronExpressionAdapter implements CronExpression {

    private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("([^ ]+ ){5}[^ ]+]"); // only 6 fields instead of 7, omitting seconds

    private final QuartzCronExpression quartzCronExpression;

    public QuartzCronExpressionAdapter(String expression) {
        if (SHORT_FORM_PATTERN.matcher(expression).matches()) {
            this.quartzCronExpression = new QuartzCronExpression("0 " + expression); // with 0 as seconds
        } else {
            this.quartzCronExpression = new QuartzCronExpression(expression);
        }
    }

    @Override
    public Instant nextAfter(Instant instant) {
        return quartzCronExpression.getNextValidTimeAfter(instant);
    }

    @Override
    public boolean matches(Instant instant) {
        return quartzCronExpression.isSatisfiedBy(instant);
    }

    @Override
    public String toString() {
        return quartzCronExpression.getCronExpression();
    }

    @Override
    public String encoded() {
        return toString();
    }

    @Override
    public Optional<ZonedDateTime> nextOccurrence(ZonedDateTime time) {
        if (!time.getZone().equals(quartzCronExpression.getTimeZone().toZoneId())) {
            QuartzCronExpression clone = new QuartzCronExpression(quartzCronExpression.getCronExpression());
            clone.setTimeZone(TimeZone.getTimeZone(time.getZone()));
            return Optional.of(ZonedDateTime.ofInstant(clone.getNextValidTimeAfter(time.toInstant()), time.getZone()));
        }
        return Optional.of(ZonedDateTime.ofInstant(quartzCronExpression.getNextValidTimeAfter(time.toInstant()), time.getZone()));
    }
}
