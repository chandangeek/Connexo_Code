/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.TimeZone;

class TypeHistRcd {

    Date evntDate;
    int evntCode;

    TypeHistoryMessageRcd historyMessage;

    static TypeHistRcd parse( Assembly assembly, TimeZone timeZone ){
        TypeHistRcd t = new TypeHistRcd( );
        t.evntDate = TypeDateTimeRcd.parse(assembly).toDate();
        t.evntCode = assembly.intValue();
        t.historyMessage = TypeHistoryMessageRcd.get(t.evntCode);
        return t;
    }

    int getEvntCode() {
        return evntCode;
    }

    Date getEvntDate() {
        return evntDate;
    }

    TypeHistoryMessageRcd getHistoryMessage() {
        return historyMessage;
    }

    /*
     * Morph a TypeHistRcd into a MeterEvent.  MeterEvents can then be added
     * to the ProfileData
     *
     * Table4 can have empty rows, there is room for 100 HIST_RCD 's but not all
     * of them have to be filled.  An empty row occurs when EVENT_CODE is 0.
     *
     * When EVENT_CODE is null historyMessage will be null.
     */
    MeterEvent toMeterEvent( ){
        MeterEvent rslt = null;

        if( historyMessage != null ) {
            int eiCode = historyMessage.getEiCode();
            String description = historyMessage.getDescription();
            rslt = new MeterEvent(evntDate, eiCode, evntCode, description );
        }
        return rslt;
    }

    public String toString( ){
        return new StringBuffer()
        .append( "TypeHistRcd [ " )
        .append( evntDate + ", " )
        .append( historyMessage )
        .append( "]" )
        .toString();
    }

}
