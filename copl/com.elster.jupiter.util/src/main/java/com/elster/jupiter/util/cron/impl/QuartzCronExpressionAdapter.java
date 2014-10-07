package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.time.ZonedDateTime;
import java.util.Date;
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
    public Date nextAfter(Date date) {
        return quartzCronExpression.getNextValidTimeAfter(date);
    }

    @Override
    public boolean matches(Date date) {
        return quartzCronExpression.isSatisfiedBy(date);
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
    public ZonedDateTime nextOccurrence(ZonedDateTime time) {
        if (!time.getZone().equals(quartzCronExpression.getTimeZone().toZoneId())) {
            QuartzCronExpression clone = new QuartzCronExpression(quartzCronExpression.getCronExpression());
            clone.setTimeZone(TimeZone.getTimeZone(time.getZone()));
            return ZonedDateTime.ofInstant(clone.getNextValidTimeAfter(Date.from(time.toInstant())).toInstant(), time.getZone());
        }
        return ZonedDateTime.ofInstant(quartzCronExpression.getNextValidTimeAfter(Date.from(time.toInstant())).toInstant(), time.getZone());
    }
}
