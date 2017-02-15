/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

/** DATE_EVENT is a record describing the mont day and year for changes
 * in SEASON, implementing special HOLIDAY schedules, and programming a 
 * RATE_RESET.  All events occur at 00:00 hours of the date specified 
 * except daylight saving time changes which occur at 02:00 of the date 
 * date specified. 
 * 
 * @author fbo
 */

class TypeDateEvent {

    TypeDateRcd date;
    int season;
    int holiday;
    int daySavingTime;
    boolean rateReset;

    static TypeDateEvent parse(Assembly assembly) {
        TypeDateEvent rcd = new TypeDateEvent();
        rcd.date = TypeDateRcd.parse(assembly);
        rcd.season = assembly.byteValue();
        rcd.holiday = assembly.byteValue();
//        String s = assembly.stringValue(1);
//        rcd.daySavingTime = "T".equals(s);
        rcd.daySavingTime = assembly.byteValue();
        String s = assembly.stringValue(1);
        rcd.rateReset = "T".equals(s);
        return rcd;
    }

    /** Date of event occurence */
    TypeDateRcd getDate() {
        return date;
    }

    /** T or F, DST in effect */
    int isDaySavingTime() {
        return daySavingTime;
    }

    /** holiday type 0 = none or 1 to MAX_DAILY_SCHEDS */
    int getHoliday() {
        return holiday;
    }

    /** T of F rate reset */
    boolean isRateReset() {
        return rateReset;
    }

    /** season in effect 1 to MAX_SEASONS */
    int getSeason() {
        return season;
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append("TypeDateEvent [ ");
        rslt.append( date.toString() );
        rslt.append( " season= " + season );
        rslt.append( " holiday= " + holiday );
        rslt.append( " daySavingTime=" + daySavingTime );
        rslt.append( " rateReset=" + rateReset );
        rslt.append(" ]");
        return rslt.toString();
    }

}
