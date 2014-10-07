package com.elster.jupiter.util.cron;

import com.elster.jupiter.util.time.ScheduleExpression;

import java.util.Date;

/**
 * Interface for classes that model a Cron expression.
 */
public interface CronExpression extends ScheduleExpression {

    /**
     * Calculates the next date after the given date that matches this CronExpression.
     * @param date
     * @return the next such Date, or null if there is no such Date.
     */
    Date nextAfter(Date date);

    /**
     * Verifies whether the given Date matches this CronExpression
     * @param date
     * @return true if the Date matches, false otherwise.
     */
    boolean matches(Date date);
}
