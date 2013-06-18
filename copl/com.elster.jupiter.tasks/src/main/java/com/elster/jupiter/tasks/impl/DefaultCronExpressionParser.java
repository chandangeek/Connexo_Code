package com.elster.jupiter.tasks.impl;

/**
 * Copyrights EnergyICT
 * Date: 18/06/13
 * Time: 9:46
 */
public class DefaultCronExpressionParser implements CronExpressionParser {

    @Override
    public CronExpression parse(String expression) {
        return new QuartzCronExpressionAdapter(expression);
    }
}
