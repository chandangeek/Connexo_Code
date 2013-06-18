package com.elster.jupiter.tasks.impl;

public interface CronExpressionParser {

    CronExpression parse(String expression);
}
