package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;

import java.util.Date;
import java.util.regex.Pattern;

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

}
