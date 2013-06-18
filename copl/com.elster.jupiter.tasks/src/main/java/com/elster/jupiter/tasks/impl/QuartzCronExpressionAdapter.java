package com.elster.jupiter.tasks.impl;

import java.util.Date;

class QuartzCronExpressionAdapter implements CronExpression {


    private final QuartzCronExpression quartzCronExpression;

    public QuartzCronExpressionAdapter(String expression) {
        this.quartzCronExpression = new QuartzCronExpression(expression);
    }

    @Override
    public Date nextAfter(Date date) {
        return quartzCronExpression.getNextValidTimeAfter(date);
    }

    @Override
    public boolean matches(Date date) {
        return quartzCronExpression.isSatisfiedBy(date);
    }
}
