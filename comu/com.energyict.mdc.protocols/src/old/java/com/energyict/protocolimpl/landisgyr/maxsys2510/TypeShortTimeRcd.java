/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

class TypeShortTimeRcd {

    int hour;
    int minute;

    static TypeShortTimeRcd parse( Assembly assembly ){
        TypeShortTimeRcd rcd = new TypeShortTimeRcd();
        rcd.hour = assembly.byteValue();
        rcd.minute = assembly.byteValue();
        return rcd;
    }
    
    /** 2 BCD digits 0 to 23 */
    int getHour() {
        return hour;
    }

    /** 2 BCD digits 0 to 59 */
    int getMinute() {
        return minute;
    }

    public String toString( ){
        return "TypeShortTimeRcd [ " + hour + ":" + minute + "]";
    }
}
