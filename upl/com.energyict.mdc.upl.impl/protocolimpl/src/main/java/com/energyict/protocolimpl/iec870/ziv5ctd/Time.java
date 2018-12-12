/*
 * Time.java
 *
 * Created on 11 april 2006, 11:28
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**  
 * The values are stored like they appear in the CP56Time.  
 * - a week starts on monday,
 * - the year has only 2 digits
 * - month is one base
 * 
 * @author fbo */

public abstract class Time implements Marshalable {
    
    int dayOfMonth;
    int dayOfWeek;
    int eti;
    int hour;
    int invalid;
    int minute;
    int month;
    int pti;
    int summerTime;
    int tarif;
    int year;
    
    Date date;
    TimeZone timeZone;

    abstract Date getDate();
    abstract String toBitString(int value, int nrBits);
    public abstract ByteArray toByteArray();
        
    /** Translate from Ziv day index to Calendar day index
     * 
     * @param dayOfWeek ziv calendar day of week
     * @return calendar day index
     */
    int toCalendarDayIndex(int dayOfWeek){
        switch(dayOfWeek) {
            case 1:
                return Calendar.MONDAY;
            case 2:
                return Calendar.TUESDAY;
            case 3:
                return Calendar.WEDNESDAY;
            case 4:
                return Calendar.THURSDAY;
            case 5:
                return Calendar.FRIDAY;
            case 6:
                return Calendar.SATURDAY;
            case 7:
                return Calendar.SUNDAY;
        }
        throw new ParseException("Could not map day of week.");
    }
    
    /** Translate from Calendar day Index to ZIV day index
     * 1 Monday, 2 Tuesday, 3 wednesday, 4 thursday, 5 friday, 6 saturday, 7 sunday 
     * 
     * @param c calendar object from wich to extract/translate day index
     * @return day index according to ziv
     */
    int toZivDayIndex(Calendar c){
        switch(c.get(Calendar.DAY_OF_WEEK) ) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            case Calendar.SUNDAY:
                return 7;
        }
        throw new ParseException("Could not map day of week.");
    }
    
    /** Translate from ziv year index to calendar year index.
     * Ziv uses 2 digits.
     *
     * @param year to translate to calendar year
     * @return the Calendar year index
     */
    int toCalendarYear(int year){
        if( year > 50 )
            return 1900+year;
        else
            return 2000+year;
    }
    
    /** Translate from calendar year index to ziv year index.
     *
     * @param year to translate into ziv yearr
     * @return ziv year index
     */
    int toZivYear(int year){
        if( year >= 2000 )
            return year-2000;
        else
            return year-1900;
    }
    
    /** Translate a calendar month index into a ziv month index
     * 
     * @param month calendar index to translate
     * @return ziv month index
     */
    int toZivMonth( int month ){
        return month + 1;
    }
    
    /** Translate a Ziv month index into a calendar month index
     * 
     * @param month ziv index 
     * @return calendar month index
     */
    int toCalendarMonth( int month ){
        return month - 1;
    }
    
}
