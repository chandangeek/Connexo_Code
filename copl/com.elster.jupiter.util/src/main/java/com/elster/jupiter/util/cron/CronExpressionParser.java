/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.cron;

import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.util.Optional;

/**
 * Interface for classes that are responsible for interpreting Strings as Cron expressions, and parsing them into a CronExpression instance.
 */
public interface CronExpressionParser extends ScheduleExpressionParser {

    /**
     * Parse the given String as a Cron expression.
     * @param expression
     * @return a Cron expression.
     */
    Optional<CronExpression> parse(String expression);
}
