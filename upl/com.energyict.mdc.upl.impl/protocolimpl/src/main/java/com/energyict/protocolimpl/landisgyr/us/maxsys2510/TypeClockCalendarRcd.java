package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.util.TimeZone;

/** @author fbo */

class TypeClockCalendarRcd {

    TypeDateTimeRcd clockCalendar;
    int timeZoneOffset;
    int dstInForce;
    int dayOfWeek;
    
    static TypeClockCalendarRcd parse( Assembly assembly, TimeZone timeZone ){
        TypeClockCalendarRcd r = new TypeClockCalendarRcd();
        r.clockCalendar = TypeDateTimeRcd.parse(assembly);
        r.timeZoneOffset = assembly.byteValue();
        r.dstInForce = assembly.byteValue();
        r.dayOfWeek = assembly.byteValue();
        return r;
    }

    TypeDateTimeRcd getClockCalendar() {
        return clockCalendar;
    }

    /**
     * @return 1 = SUN., 7 = SAT. READ-ONLY
     */
    int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * @return DST on? controlled by date table.  This flg is set in 
     * each table entry for which DST is in effect. READ-ONLY
     */
    int getDstInForce() {
        return dstInForce;
    }

    /**
     * @return From the Central computer Site in hours, 2 BCD positive value 
     * digits, the ms bit is the sign bit, 0 = positive, 1 = negative
     */
    int getTimeZoneOffset() {
        return timeZoneOffset;
    }
    
    public String toString( ){
        return new StringBuffer()
        .append( "TypeClockCalendar [ " )
        .append( clockCalendar + " " )
        .append( "dow " + dayOfWeek + " " )
        .append( "GMT+" + timeZoneOffset + " " )
        .append( "DST" + dstInForce + " " )
        .append( "]" )
        .toString();
    }

    public static void main(String [] args){
        /*
        ByteArray ba = new ByteArray( new byte [] { 0x04, 0x25, 0x06, 0x15, 0x00, 0x49, 0x00, 0x00, 0x03, 0x00 } );
       // Assembly a = new Assembly( ba );
      
        System.out.println( 
        TypeClockCalendarRcd.parse( a, TimeZone.getDefault() ));
      */  
    }
    
}
