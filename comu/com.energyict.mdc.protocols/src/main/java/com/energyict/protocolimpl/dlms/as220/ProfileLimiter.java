/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220;

import java.util.Calendar;
import java.util.Date;

public class ProfileLimiter {

    private final Date fromDate;
    private final Date toDate;
    private final Date oldFromDate;
    private final Date oldToDate;
    private final int limitMaxNrOfDays;

    /**
     * AM500 add a new "LimitMaxNrOfDays" property to the AS220 & GasDevice protocol.<p>
     * This property is optional, and if exists, it should contain a integer (positive or negative) describing a number of days.
     * <p>
     * The protocol should read the following part of the profileData:<p>
     * <p><b>When "LimitMaxNrOfDays" does not exist or "LimitMaxNrOfDays" value == 0
     * <pre>
     *  FROM [lastReading] TO [now]
     * </pre>
     * <p><b>When "LimitMaxNrOfDays" property > 0</b>
     * <pre>
     *  FROM [lastReading] TO [lastReading + nrOfDays]
     *  if [lastReading + nrOfDays] > now, we use now as toDate.
     * </pre>
     * <p><b>When "LimitMaxNrOfDays" property < 0
     * <pre>
     *  FROM [now - |nrOfDays|] TO [now]
     *  if [now - |nrOfDays|] < lastReading, we use the lastReading as fromdate.
     * </pre>
     * <p>
     * <lh>Keep in mind that:<lh>
     * <li>When "LimitMaxNrOfDays" property > 0 No gaps in channelData</li>
     * <li>When "LimitMaxNrOfDays" property < 0 There will be gaps in channelData if lastReading < (now - nrOfDays)</li>
     * <p>
     * @param from, the date as the original fromDate used to call the getProfileData method
     * @param to, the date as the original fromDate used to call the getProfileData method
     * @param limitMaxNrOfDays, the LimitMaxNrOfDays property of the protocol
     */
    public ProfileLimiter(Date from, Date to, int limitMaxNrOfDays) {
        this.oldFromDate = from;
        this.oldToDate = to;
        this.limitMaxNrOfDays = limitMaxNrOfDays;


        if (limitMaxNrOfDays > 0) {
            Calendar toCalendar = Calendar.getInstance();
            toCalendar.setTime(from);
            toCalendar.add(Calendar.DAY_OF_MONTH, limitMaxNrOfDays);
            if (toCalendar.getTime().after(to)) {
                toCalendar.setTime(to);
            }
            toDate = toCalendar.getTime();
            fromDate = from;
        } else if (limitMaxNrOfDays < 0) {
            Calendar fromCalendar = Calendar.getInstance();
            fromCalendar.setTime(to);
            fromCalendar.add(Calendar.DAY_OF_MONTH, limitMaxNrOfDays);
            if (fromCalendar.getTime().before(from)) {
                fromCalendar.setTime(from);
            }
            this.fromDate = fromCalendar.getTime();
            this.toDate = to;
        } else {
            this.fromDate = from;
            this.toDate = to;
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
     * The new toDate, calculated using the limitMaxNrOfDays value given in the constructor
     * @return
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Getter for the original fromDate, provided in the constructor
     * @return
     */
    public Date getOldFromDate() {
        return oldFromDate;
    }

    /**
     * Getter for the original toDate, provided in the constructor
     * @return
     */
    public Date getOldToDate() {
        return oldToDate;
    }

    /**
     * Getter for the limitMaxNrOfDays, provided in the constructor
     * @return
     */
    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getLimitMaxNrOfDays() == 0) {
            sb.append("ProfileLimiter not active (limitMaxNrOfDays == 0)");
        } else {
            if (!getOldFromDate().equals(getFromDate())) {
                sb.append("ProfileLimiter changed fromDate from [").append(getOldFromDate()).append("] to [").append(fromDate).append("]. ");
            }
            if (!getOldToDate().equals(getToDate())) {
                sb.append("ProfileLimiter changed toDate from [").append(getOldToDate()).append("] to [").append(toDate).append("]. ");
            }
        }
        return sb.toString();
    }
}
