/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

/**
 * Cyclic rate resets can repeat on a number of days cycle, 1..N, a day of week 
 * cycle, 1..7, or a day of month cycle, 1..28. The hour is specified.
 * 
 * CYCLIC_RATE_RESET: provides a means of automatically resetting the 
 * current data on a cyclical basis.  Just before the reset, the Current
 * Data is copied into a Self-Read data area so that the billing data can be
 * recovered at a later date.  The resetting cycle can repeat on a number of 
 * days cycle, 1..N, a day of week cycle, 1..7, a day of month cycle, 1..28,
 * or number of hours cycle, 01, 99.  for the first three types of reset, the 
 * hour of the reset and the HOUR field contains the number of hours for cyclic
 * reset (01..99).
 * 
 * The code for determining the cycle is as follows:
 * - by number of days 0
 * - by day of week 1
 * - by day of month 2
 * - by number of hours 3
 * 
 * @author fbo
 */

class TypeCyclicRateReset {

    public final static int NUMBER_OF_DAYS = 0;
    public final static int DAY_OF_WEEK = 1;
    public final static int DAY_OF_MONTH = 2;
    public final static int NUMBER_OF_HOURS = 3; 
    
    int resetPeriod;
    int day;
    int hour;

    static TypeCyclicRateReset parse(Assembly assembly) {
        TypeCyclicRateReset rcd = new TypeCyclicRateReset();
        rcd.resetPeriod = assembly.intValue();
        rcd.day = assembly.byteValue();
        rcd.hour = assembly.byteValue();
        return rcd;
    }

    int getResetPeriod() {
        return resetPeriod;
    }
    
    int getDay() {
        return day;
    }

    int getHour() {
        return hour;
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append("TypeCyclicRateReset [ ");
        switch( resetPeriod ){
            case NUMBER_OF_DAYS: rslt.append( "Number of days " ); break;
            case DAY_OF_WEEK: rslt.append( "Day of Week " ); break;
            case DAY_OF_MONTH: rslt.append( "Day of month " ); break;
            case NUMBER_OF_HOURS: rslt.append( "Number of hours " ); break;
        }
        
        rslt.append( "\n" );
        rslt.append( "type=" + resetPeriod + " ");
        rslt.append( "day= " + day + " " );
        rslt.append( "hour= " + hour + " "  );
        rslt.append( "]" );
        
        return rslt.toString();
    }
}
