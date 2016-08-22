package com.elster.us.protocolimpl.landisgyr.quad4;

import java.util.TimeZone;

class Table1 {

    TypeClockCalendarRcd typeClockCalendarRcd;
    
    static Table1 parse( Assembly a ){
        Table1 t = new Table1();
        TimeZone tZone = a.getQuad4().getTimeZone();
        t.typeClockCalendarRcd = TypeClockCalendarRcd.parse( a, tZone );
        return t;
    }
    
    TypeClockCalendarRcd getTypeMaximumValues( ){
        return typeClockCalendarRcd;
    }
    
}
