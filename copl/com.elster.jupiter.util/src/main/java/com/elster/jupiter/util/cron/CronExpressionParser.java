package com.elster.jupiter.util.cron;

public interface CronExpressionParser {

    CronExpression parse(String expression);
}
