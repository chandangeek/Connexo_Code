/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.interval.PartialTime;

import java.util.Calendar;

import static com.elster.jupiter.util.Checks.is;

/**
 * Models a window during which communication with a device is allowed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (17:02)
 */
public class ComWindow {

    private PartialTime start;
    private PartialTime end;

    public ComWindow() {
        this(PartialTime.fromSeconds(0), PartialTime.fromHours(24));
    }

    public ComWindow (PartialTime start, PartialTime end) {
        super();
        this.start = start;
        this.end = end;
    }

    public ComWindow (int startSeconds, int endSeconds) {
        this(PartialTime.fromSeconds(startSeconds), PartialTime.fromSeconds(endSeconds));
    }

    /**
     * Gets the start of the communication window within a day boundary.
     *
     * @return The start of the communication window
     */
    public PartialTime getStart () {
        return start;
    }

    /**
     * Gets the end of the communication window within a day boundary.
     *
     * @return The end of the communication window
     */
    public PartialTime getEnd () {
        return end;
    }

    /**
     * Tests if a TimeDuration is within the ComWindow.
     * Example: A ComWindow from 1 AM to 6 AM includes new TimeDuration(3, TimeDuration.HOURS)
     *
     * @param timeDuration The TimeDuration
     * @return A flag that indicates if the TimeDuration is included in this ComWindow
     */
    public boolean includes (TimeDuration timeDuration) {
        if (this.getStart().before(this.getEnd())) {
            return this.includes(timeDuration, this.getStart(), this.getEnd());
        }
        else {
            return !this.includes(
                        timeDuration,
                        this.getEnd().plus(PartialTime.fromMilliSeconds(1)),
                        this.getStart().plus(PartialTime.fromMilliSeconds(-1)));
        }
    }

    private boolean includes (TimeDuration timeDuration, PartialTime start, PartialTime end) {
        PartialTime timeDurationFromMidnight = PartialTime.fromSeconds(timeDuration.getSeconds());
        // Verify that start <= timeDurationFromMidnight <= end
        return !timeDurationFromMidnight.before(start)
                && !end.before(timeDurationFromMidnight);
    }

    /**
     * Tests if the time stamp representend by the Calendar is within the ComWindow.
     *
     * @param calendar The Calendar
     * @return A flag that indicates if the Calendar is included in this ComWindow
     */
    public boolean includes (Calendar calendar) {
        PartialTime secondsFromMidnight = this.secondsFromMidnight(calendar);
        return !this.getEnd().before(secondsFromMidnight)    // Equals secondsFromMidnight <= end
            && !secondsFromMidnight.before(this.getStart()); // Equals secondsFromMidnight >= start
    }

    private PartialTime secondsFromMidnight (Calendar calendar) {
        long millisBeforeTruncate = calendar.getTimeInMillis();
        Calendar forTruncationPurposesOnly = Calendar.getInstance();
        forTruncationPurposesOnly.setTimeInMillis(millisBeforeTruncate);
        new TimeDuration(1, TimeDuration.TimeUnit.DAYS).truncate(forTruncationPurposesOnly);
        long millisAfterTruncate = forTruncationPurposesOnly.getTimeInMillis();
        long millisFromMidnight = millisBeforeTruncate - millisAfterTruncate;
        return PartialTime.fromMilliSeconds((int) millisFromMidnight);
    }

    /**
     * Tests if this ComWindow starts after the time stamp
     * represented by the Calender.
     *
     * @param calendar The Calendar
     * @return A flag that indicates if this ComWindow starts after the time stamp represented by the Calendar
     */
    public boolean after (Calendar calendar) {
        PartialTime secondsFromMidnight = this.secondsFromMidnight(calendar);
        return secondsFromMidnight.before(this.getStart());
    }

    /**
     * Tests if this ComWindow is empty.
     * This is the case if the start is equal to the end
     * or in other words, no time elapses between the
     * start and the end of this ComWindow.
     *
     * @return A flag that indicates if this ComWindow is empty
     */
    public boolean isEmpty() {
        return is(this.start).equalTo(this.end);
    }

    @Override
    public boolean equals (Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject instanceof ComWindow) {
            ComWindow that = (ComWindow) anotherObject;
            return this.start.equals(that.start) && this.end.equals(that.end);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode () {
        int result = this.start.hashCode();
        result = 31 * result + this.end.hashCode();
        return result;
    }

    public String toString () {
        return "between " + this.getStart().toString() + " and " + this.getEnd().toString();
    }

}
