package com.energyict.mdc.device.config;

import com.energyict.mdc.common.TimeDuration;
import org.joda.time.DateTimeConstants;

import java.util.Calendar;
import java.util.Date;

/**
 * TemporalExpression represent a recurring time.
 * Examples:
 * <ul>
 * <li>each day at 6:00</li>
 * <li>the third day of each second month</li>
 * </ul>
 *
 * @author Karel
 */
public class TemporalExpression {

    private static final int MAXIMUM_NUMBER_OF_DAYS_IN_ALL_MONTHS = 28;
    private static final int NUMBER_OF_SECONDS_IN_MAXIMUM_DAYS_IN_ALL_MONTHS = DateTimeConstants.SECONDS_PER_DAY * MAXIMUM_NUMBER_OF_DAYS_IN_ALL_MONTHS;

    private TimeDuration offset;
    private TimeDuration every;

    /**
     * Creates a new instance of TemporalExpression.
     *
     * @param every  the frequency of the new TemporalExpression
     * @param offset the offset to apply to the frequency
     */
    public TemporalExpression(TimeDuration every, TimeDuration offset) {
        this();
        this.every = every;
        this.offset = offset;
    }

    /**
     * Creates a new TemporalExpression.
     *
     * @param every the frequency of the new TemporalExpression
     */
    public TemporalExpression(TimeDuration every) {
        this(every, new TimeDuration());
    }

    // For orm framework only
    private TemporalExpression() {
        super();
    }

    /**
     * Gets the receiver's every field.
     *
     * @return the frequency of the receiver
     */
    public TimeDuration getEvery() {
        return every;
    }

    /**
     * Gets the receiver's offset field.
     *
     * @return the receiver's offset
     */
    public TimeDuration getOffset() {
        return offset;
    }

    /**
     * Returns a string representation of the receiver.
     *
     * @return the string representation
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder("every ");
        buffer.append(every);
        if (offset != null && !offset.isEmpty()) {
            buffer.append(" (offset: ");
            buffer.append(offset);
            buffer.append(")");
        }
        return buffer.toString();
    }

    /**
     * Returns the first occurrence of the receiver later than the argument.
     *
     * @param previous calendar specifying the start Date
     * @return the Date of the next occurrence
     */
    public Date nextOccurrence(Calendar previous) {
        Calendar base = (Calendar) previous.clone();
        base.setLenient(true);

        if (every.getTimeUnitCode() == offset.getTimeUnitCode()) {
            every.addTo(base, true);
        }
        every.truncate(base);
        TimeDuration offset = this.offset;
        if (this.indicatesLastOfMonth()) {
            offset = this.offsetToLastOfMonth(base, this.offset);
        }
        offset.addTo(base, true);

        if (!base.getTime().after(previous.getTime())) {
            every.addTo(base, false);
        }

        return base.getTime();
    }

    private TimeDuration offsetToLastOfMonth (Calendar base, TimeDuration offset) {
        /* Base will already be set to the first of the month
         * so to get to midnight of the last day,
         * we need to add max - 1 days to the first day of the month. */
        int lastDayOfMonth = base.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;
        int remainingSecondsInDay = offset.getSeconds() % DateTimeConstants.SECONDS_PER_DAY;
        if (remainingSecondsInDay > 0) {
            return new TimeDuration(lastDayOfMonth * DateTimeConstants.SECONDS_PER_DAY + remainingSecondsInDay, TimeDuration.SECONDS);
        }
        else {
            return new TimeDuration(lastDayOfMonth, TimeDuration.DAYS);
        }
    }

    private boolean indicatesLastOfMonth () {
        return every.getTimeUnitCode() == TimeDuration.MONTHS
            && offset.getSeconds() > NUMBER_OF_SECONDS_IN_MAXIMUM_DAYS_IN_ALL_MONTHS;
    }


    /**
     * Tests whether the next occurence based on the argument,
     * is earlier than the current time.
     *
     * @param previous calendar specifying the start Date
     * @return true if the next occurence is earlier than the current time
     */
    public boolean hasExpired(Calendar previous) {
        return nextOccurrence(previous).before(new Date());
    }

}