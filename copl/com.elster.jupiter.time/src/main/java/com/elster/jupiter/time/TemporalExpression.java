package com.elster.jupiter.time;

import com.elster.jupiter.util.time.ScheduleExpression;

import org.joda.time.DateTimeConstants;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

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
public final class TemporalExpression implements ScheduleExpression {

    private static final int SECONDS_PER_DAY = (int) ChronoUnit.DAYS.getDuration().getSeconds();
    private static final int MAXIMUM_NUMBER_OF_DAYS_IN_ALL_MONTHS = 28;
    private static final int NUMBER_OF_SECONDS_IN_MAXIMUM_DAYS_IN_ALL_MONTHS = SECONDS_PER_DAY * MAXIMUM_NUMBER_OF_DAYS_IN_ALL_MONTHS;
    private static final int SECONDS_IN_MINUTE = DateTimeConstants.SECONDS_PER_MINUTE;

    private TimeDuration offset;
    private TimeDuration every;

    public static class Offsets {
        private int days = 0;
        private int hours = 0;
        private int minutes = 0;

        public static Offsets from(int days, int hours, int minutes) {
            Offsets offsets = new Offsets();
            offsets.days = days;
            offsets.hours = hours;
            offsets.minutes = minutes;
            return offsets;
        }

        public int getDays() {
            return days;
        }

        public int getHours() {
            return hours;
        }

        public int getMinutes() {
            return minutes;
        }

    }

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
        this(every, new TimeDuration(0, TimeDuration.TimeUnit.SECONDS));
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

    public Offsets getOffsetInDaysHoursMinutes() {
        TimeDuration offsetTD = getOffset();
        int offsetInMinutes = offsetTD==null ? 0 : offsetTD.getCount();
        if (offsetTD!=null && offsetTD.getTimeUnitCode() != Calendar.MINUTE) {
            offsetInMinutes = offsetTD.getSeconds() / SECONDS_IN_MINUTE;
        }
        int days = 0;
        int hours = 0;
        int minutes = 0;
        switch (this.every.getTimeUnitCode()) {
            case Calendar.MONTH:
            case Calendar.WEEK_OF_YEAR:
                if (offsetInMinutes >= DateTimeConstants.MINUTES_PER_DAY) {
                    days = offsetInMinutes / DateTimeConstants.MINUTES_PER_DAY;
                    offsetInMinutes -= (days * DateTimeConstants.MINUTES_PER_DAY);
                }
                // NO BREAK! (intentionally)
            case Calendar.DATE:
                if (offsetInMinutes >= DateTimeConstants.MINUTES_PER_HOUR) {
                    hours = offsetInMinutes / DateTimeConstants.MINUTES_PER_HOUR;
                    offsetInMinutes -= (hours * DateTimeConstants.MINUTES_PER_HOUR);
                }
                // NO BREAK! (intentionally)
            case Calendar.HOUR_OF_DAY:
                minutes = offsetInMinutes;
                break;

            case Calendar.MINUTE:
                minutes = offsetInMinutes;
                break;
        }
        return Offsets.from(days, hours, minutes);
    }

    public String toCronExpression(TimeZone targetTimeZone, TimeZone definitionTimeZone) {
        final TimeDuration every = this.getEvery();
        final Offsets offset = this.getOffsetInDaysHoursMinutes();

        Calendar localTime = Calendar.getInstance();
        localTime.setTimeZone(definitionTimeZone);
        if (every.getTimeUnitCode() == Calendar.MONTH){
            localTime.set(Calendar.DAY_OF_MONTH, offset.getDays() + 1); //+1 because EiServer has 0-based days
        } else if (every.getTimeUnitCode() == Calendar.WEEK_OF_YEAR){
            localTime.set(Calendar.DAY_OF_WEEK, offset.getDays() + 1);
        }
        localTime.set(Calendar.HOUR_OF_DAY, offset.getHours());
        localTime.set(Calendar.MINUTE, offset.getMinutes());

        //convert localTime to beaconTime
        Calendar beaconCalendar = Calendar.getInstance();
        beaconCalendar.setTimeZone(targetTimeZone);
        beaconCalendar.setTimeInMillis(localTime.getTimeInMillis());
        int dayOfMonth = beaconCalendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = beaconCalendar.get(Calendar.DAY_OF_WEEK);
        int hours = beaconCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = beaconCalendar.get(Calendar.MINUTE);

        switch (every.getTimeUnitCode()) {
            case Calendar.MONTH:
                return "0 " + minutes + " " + hours + " " + dayOfMonth + " */" + every.getCount() + " *";  //E.g. '0 0 6 16 */3 *' means 'every 3 months, on day 16, at 06:00:00'
            case Calendar.WEEK_OF_YEAR:
                return "0 " + minutes + " " + hours + " */" + (every.getCount() * 7) + " * " + dayOfWeek;  //E.g. '0 0 6 */14 * 1' means 'every 2 weeks, on monday, at 06:00:00'
            case Calendar.DATE:
                return "0 " + minutes + " " + hours + " */" + every.getCount() + " * *";  //E.g. '0 0 6 */3 * *' means 'every 3 days, at 06:00:00'
            case Calendar.HOUR:
                return "0 " + offset.getMinutes() + " */" + every.getCount() + " * * *";  //E.g. '0 3 */2 * * *' means 'every 2 hours, at 00:03:00'
            case Calendar.MINUTE:
                return "0 */" + every.getCount() + " * * * *";  //E.g. '0 */5 * * * *' means 'every 5 minutes'
            default:
                return "";
        }
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
            if (previous.get(Calendar.DAY_OF_MONTH) == previous.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                base.add(Calendar.MONTH, 1);
            }
            offset = this.offsetToLastOfMonth(base, this.offset);
        }
        offset.addTo(base, true);

        if (!base.getTime().after(previous.getTime())) {
            every.addTo(base, false);
        }

        return base.getTime();
    }

    private TimeDuration offsetToLastOfMonth(Calendar base, TimeDuration offset) {
        /** Base will already be set to the first of the month
         * so to get to midnight of the last day,
         * we need to add max - 1 days to the first day of the month.
         **/
        int lastDayOfMonth = base.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;
        int remainingSecondsInDay = offset.getSeconds() % SECONDS_PER_DAY;
        if (remainingSecondsInDay > 0) {
            return new TimeDuration(lastDayOfMonth * SECONDS_PER_DAY + remainingSecondsInDay, TimeDuration.TimeUnit.SECONDS);
        } else {
            return new TimeDuration(lastDayOfMonth, TimeDuration.TimeUnit.DAYS);
        }
    }

    private boolean indicatesLastOfMonth() {
        return every.getTimeUnit() == TimeDuration.TimeUnit.MONTHS
                && offset.getSeconds() >= NUMBER_OF_SECONDS_IN_MAXIMUM_DAYS_IN_ALL_MONTHS;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemporalExpression)) {
            return false;
        }

        TemporalExpression that = (TemporalExpression) o;

        return every.equals(that.every) && offset.equals(that.offset);

    }

    @Override
    public int hashCode() {
        return Objects.hash(every, offset);
    }

    public boolean isLastDay() {
        return this.indicatesLastOfMonth();
    }

    public void setLastDay() {
        if (this.every.getTimeUnit() != TimeDuration.TimeUnit.MONTHS) {
            throw new IllegalStateException("Can only switch to lastOfMonth is period is monthly");
        }
        if (!this.indicatesLastOfMonth()) {
            offset = new TimeDuration(this.offset.getSeconds() + NUMBER_OF_SECONDS_IN_MAXIMUM_DAYS_IN_ALL_MONTHS, TimeDuration.TimeUnit.SECONDS);
        }
    }

    @Override
    public Optional<ZonedDateTime> nextOccurrence(ZonedDateTime time) {
        ZonedDateTime result = time;

        result = every.getTimeUnit().truncate(result);
        if (indicatesLastOfMonth()) {
            if (ChronoField.DAY_OF_MONTH.rangeRefinedBy(time).getMaximum() == time.getDayOfMonth()) {
                result = result.plusMonths(1);
            }
            result = result.with(ChronoField.DAY_OF_MONTH, ChronoField.DAY_OF_MONTH.rangeRefinedBy(result).getMaximum());
        } else {
            result = result.plus(offset.getCount(), offset.getTemporalUnit());
        }
        while (!result.isAfter(time)) {
            result = result.plus(every.getCount(), every.getTemporalUnit());
        }
        return Optional.of(result);
    }

    @Override
    public String encoded() {
        return encode(every) + ';' + (offset == null ? "" : encode(offset));
    }

    private String encode(TimeDuration timeDuration) {
        return timeDuration.getCount() + "," + timeDuration.getTimeUnit().getCode();
    }
}