package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import com.energyict.protocols.util.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** @author fbo */

class TypeDateTimeRcd {

    TimeZone timeZone;
    int month;
    int day;
    int year;
    int hour;
    int minute;
    int second;

    static TypeDateTimeRcd parse(Assembly assembly){
        TypeDateTimeRcd r = new TypeDateTimeRcd( );
        r.timeZone = assembly.getMaxSys().getTimeZone();
        r.month = assembly.byteValue();
        r.day = assembly.byteValue();
        r.year = assembly.byteValue();
        r.hour = assembly.byteValue();
        r.minute = assembly.byteValue();
        r.second = assembly.byteValue();
        return r;
    }

    /** 2 BCD digits 1 to 12 */
    int getMonth(){
        return month;
    }

    /** 2 BCD digits 1 to 31 */
    int getDay(){
        return day;
    }

    /** 2 BCD digits 00 to 99 */
    int getYear(){
        return year;
    }

    /** 2 BCD digits 0 to 23 */
    int getHour(){
        return hour;
    }

    /** 2 BCD digits 0 to 59 */
    int getMinute(){
        return minute;
    }

    /** 2 BCD digits 0 to 59 */
    int getSecond(){
        return second;
    }

    Date toDate(){

        int cYear;
        if( year > 50 )
            cYear = 1900+year;
        else
            cYear = 2000+year;

        Calendar c = ProtocolUtils.getCalendar( timeZone );
        c.set( Calendar.YEAR, cYear );
        c.set( Calendar.MONTH, month-1 );
        c.set( Calendar.DAY_OF_MONTH, day );
        c.set( Calendar.HOUR_OF_DAY, hour);
        c.set( Calendar.MINUTE, minute);
        c.set( Calendar.SECOND, second);
        c.set( Calendar.MILLISECOND, 0);
        return c.getTime();

    }

    public String toString( ){
        return new StringBuffer()
        .append( "TypeDateTimeRcd[ " )
        .append( day + "/" + month + "/" + year + " " )
        .append( hour + ":" + minute + ":" + second + "]" )
        .toString();
    }

}
