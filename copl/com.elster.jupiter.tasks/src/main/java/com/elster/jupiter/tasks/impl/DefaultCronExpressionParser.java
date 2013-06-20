package com.elster.jupiter.tasks.impl;

public class DefaultCronExpressionParser implements CronExpressionParser {

    @Override
    public CronExpression parse(String expression) {
        return new QuartzCronExpressionAdapter(expression);
    }
}
