package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/14
 * Time: 16:12
 */
public class ProfileLimiter {

    private final Date fromDate;
    private final Date toDate;
    private final Date originalFromDate;
    private final Date originalToDate;
    private final int limitMaxNrOfDays;
    private final TimeZone timeZone;
    /**
     * <p>
     * The protocol should read the following part of the profileData:
     * <p><b>When "LimitMaxNrOfDays" does not exist or "LimitMaxNrOfDays" value == 0
     * <pre>
     *  FROM [lastReading] TO [now]
     * </pre>
     * <b>When "LimitMaxNrOfDays" property > 0</b>
     * <pre>
     *  FROM [lastReading] TO [lastReading + nrOfDays]
     *  if [lastReading + nrOfDays] > now, we use now as toDate.
     * </pre>
     * <b>When "LimitMaxNrOfDays" property < 0
     * <pre>
     *  FROM [now - |nrOfDays|] TO [now]
     *  if [now - |nrOfDays|] < lastReading, we use the lastReading as fromdate.
     * </pre>
     * <b>REMARK 1:</b> As profile data can only be read in blocks of 24 hours, above dates are rounded towards midnight.
     * <p>
     * <b>REMARK 2:</b> Keep in mind that:
     * <li>When "LimitMaxNrOfDays" property > 0 No gaps in channelData</li>
     * <li>When "LimitMaxNrOfDays" property < 0 There will be gaps in channelData if lastReading < (now - nrOfDays)</li>
     * <p>
     * @param from, the date as the original fromDate used to call the getProfileData method
     * @param to, the date as the original fromDate used to call the getProfileData method
     * @param limitMaxNrOfDays, the LimitMaxNrOfDays property of the protocol
     */
    public ProfileLimiter(Date from, Date to, int limitMaxNrOfDays, TimeZone timeZone) {
        this.originalFromDate = from;
        this.originalToDate = to;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
        this.timeZone = timeZone;
        Calendar fromCalendar = Calendar.getInstance(timeZone);
        Calendar toCalendar = Calendar.getInstance(timeZone);

        if (limitMaxNrOfDays > 0) {
            fromCalendar.setTime(from);
            toCalendar.setTime(from);
            toCalendar.add(Calendar.DAY_OF_MONTH, limitMaxNrOfDays);
            if (toCalendar.getTime().after(to)) {
                toCalendar.setTime(to);
                roundCalendarToMidnight(toCalendar, true);
            } else {
                roundCalendarToMidnight(toCalendar, false);
            }
            roundCalendarToMidnight(fromCalendar, false);
            fromDate = fromCalendar.getTime();
            toDate = toCalendar.getTime();
        } else if (limitMaxNrOfDays < 0) {
            toCalendar.setTime(to);
            fromCalendar.setTime(to);
            fromCalendar.add(Calendar.DAY_OF_MONTH, limitMaxNrOfDays);
            if (fromCalendar.getTime().before(from)) {
                fromCalendar.setTime(from);
                roundCalendarToMidnight(fromCalendar, false);
            } else {
                roundCalendarToMidnight(fromCalendar, true);
            }
            roundCalendarToMidnight(toCalendar, true);
            this.fromDate = fromCalendar.getTime();
            this.toDate = toCalendar.getTime();
        } else {
            fromCalendar.setTime(from);
            toCalendar.setTime(to);
            roundCalendarToMidnight(fromCalendar, false);
            roundCalendarToMidnight(toCalendar, true);
            this.fromDate = fromCalendar.getTime();
            this.toDate = toCalendar.getTime();
        }
    }

    /**
     * The new fromDate, calculated using the limitMaxNrOfDays value given in the constructor
     * @return
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * The Calendar containing the new fromDate, calculated using the limitMaxNrOfDays value given in the constructor and rounded to midnight
     * @return
     */
    public Calendar getFromCalendar() {
        Calendar toCalendar = Calendar.getInstance(timeZone);
        toCalendar.setTime(getFromDate());
        return toCalendar;
    }

    /**
     * The Calendar containing the new toDate, calculated using the limitMaxNrOfDays value given in the constructor and rounded to midnight
     * @return
     */
    public Date getToDate() {
        return toDate;
    }

    public Calendar getToCalendar() {
        Calendar toCalendar = Calendar.getInstance(timeZone);
        toCalendar.setTime(getToDate());
        return toCalendar;
    }

    /**
     * Getter for the original fromDate, provided in the constructor
     * @return
     */
    public Date getOriginalFromDate() {
        return originalFromDate;
    }

    /**
     * Getter for the original toDate, provided in the constructor
     * @return
     */
    public Date getOriginalToDate() {
        return originalToDate;
    }

    /**
     * Getter for the limitMaxNrOfDays, provided in the constructor
     * @return
     */
    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

    public static Calendar roundCalendarToMidnight(Calendar cal, boolean roundUp) {
        if (roundUp) {
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getLimitMaxNrOfDays() == 0) {
            sb.append("ProfileLimiter not active (limitMaxNrOfDays == 0)");
        } else {
            if (!getOriginalFromDate().equals(getFromDate())) {
                sb.append("ProfileLimiter changed fromDate from [").append(getOriginalFromDate()).append("] to [").append(fromDate).append("]. ");
            }
            if (!getOriginalToDate().equals(getToDate())) {
                sb.append("ProfileLimiter changed toDate from [").append(getOriginalToDate()).append("] to [").append(toDate).append("]. ");
            }
        }
        return sb.toString();
    }
}
