/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

class TypeDateRcd {

    int month;
    int day;
    int year;
    int fill;

    static TypeDateRcd parse( Assembly assembly ){
        TypeDateRcd rcd = new TypeDateRcd();
        rcd.month = assembly.byteValue() & 0xFF;
        rcd.day = assembly.byteValue() & 0xFF;
        rcd.year = assembly.byteValue() & 0xFF;
        rcd.fill = assembly.byteValue() & 0xFF;
        return rcd;
    }
    
    /** 2 BCD digits 1 to 31 */
    int getDay() {
        return day;
    }

    int getFill() {
        return fill;
    }

    /** 2 BCD digits 1 to 12 */
    int getMonth() {
        return month;
    }

    /** 2 BCD digits 0 to 99 */    
    int getYear() {
        return year;
    }
    
    public String toString( ){
        return "TypeDateRcd [ month " + month + " day " + day + " year " + year + " fill " + fill + " ]";
    }
}
