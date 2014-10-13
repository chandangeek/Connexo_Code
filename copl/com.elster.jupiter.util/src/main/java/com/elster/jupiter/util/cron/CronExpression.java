package com.elster.jupiter.util.cron;

import java.time.Instant;


/**
 * Interface for classes that model a Cron expression.
 */
public interface CronExpression {

    /**
     * Calculates the next date after the given date that matches this CronExpression.
     * @param date
     * @return the next such Date, or null if there is no such Date.
     */
    Instant nextAfter(Instant date);

    /**
     * Verifies whether the given Date matches this CronExpression
     * @param date
     * @return true if the Date matches, false otherwise.
     */
    boolean matches(Instant date);
}
