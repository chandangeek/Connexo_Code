package com.elster.jupiter.util.cron;

/**
 * Interface for classes that are responsible for interpreting Strings as Cron expressions, and parsing them into a CronExpression instance.
 */
public interface CronExpressionParser {

    /**
     * Parse the given String as a Cron expression.
     * @param expression
     * @return a Cron expression.
     */
    CronExpression parse(String expression);
}
