package com.elster.jupiter.util.time;

import java.util.Date;
import java.util.TimeZone;

/**
 * Abstraction of any method of getting information about the current time and time zone.
 * It is preferred that methods that need to get hold of the current time go through an instance of Clock, rather than calling new Date() or System.currentTimeMillis().
 * This allows tests to verify behavior independent of actual time by supplying mock instances of Clock.
 */
public interface Clock {

    /**
     * @return the System's current time zone.
     */
    TimeZone getTimeZone();

    /**
     * @return a Date instance representing the System's current time.
     */
    Date now();

}
