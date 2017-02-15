/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/** @author fbo */

public class HistoricalEventRegister {

    private static final int HISTORY_SIZE = 3;

    private int reverseRunCount;
    private int phaseFailureCount;
    private int powerFailCount;
    private int programmingCount;
    private int ctRatioCount;
    private int billingCount;
    private int transientResetCount;
    private int meterErrorCount;
    private int terminalCoverCount;
    private int mainCoverCount;
    private int internalBatteryCount;
    private int externalBatteryCount;

    private ArrayList events = new ArrayList();
    private TimeZone timeZone;

    /** Creates a new instance of HistoricalEventRegister */
    public HistoricalEventRegister(byte[] data, TimeZone timeZone) throws IOException {
        this.timeZone=timeZone;
        parse(data);
    }

    public Collection getEvents(){
        return events;
    }

    private void parse(byte[] data) throws IOException {

        long shift;
        Date date;
        reverseRunCount = ProtocolUtils.getIntLE(data, 0, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 2 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            events.add( new MeterEvent(date, MeterEvent.REVERSE_RUN, "Reverse Running") );
        }

        phaseFailureCount = ProtocolUtils.getIntLE(data, 14, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 16 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            int phase = ProtocolUtils.getIntLE(data, 28+(1*i), 1 );
            String dscr = "Phase failure";
            if( (phase&0x01) > 0 ) dscr += " Phase A";
            if( (phase&0x02) > 0 ) dscr += " Phase B";
            if( (phase&0x04) > 0 ) dscr += " Phase C";
            events.add( new MeterEvent(date, MeterEvent.PHASE_FAILURE, dscr ) );
        }

        powerFailCount = ProtocolUtils.getIntLE(data, 31, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 33 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            events.add( new MeterEvent(date, MeterEvent.POWERDOWN, "Power down") );
        }

        programmingCount = ProtocolUtils.getIntLE(data, 45, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 47 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            String userName = new String( ProtocolUtils.getSubArray2( data, 62+(12*i), 12 )  ).trim();
            int prSrc = ProtocolUtils.getIntLE(data, 59+(1*i), 1 );
            String dscr = "Configuration change user: " + userName;
            dscr += (prSrc==0) ? ", optical cummunications port" : ", remote communications port";
            MeterEvent me = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, dscr);
            events.add(me);
        }

        ctRatioCount = ProtocolUtils.getIntLE(data, 98, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 102 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, "Configuration change");
            events.add(me);
        }

        billingCount = ProtocolUtils.getIntLE(data, 151, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 153 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            int src = ProtocolUtils.getIntLE(data, 165+(1*i), 1 );
            String dscr = "Billing action"  ;
            dscr += (src==0) ? ", optical cummunications port" : ", remote communications port";
            MeterEvent me = new MeterEvent(date, MeterEvent.BILLING_ACTION, dscr);
            events.add(me);
        }

        transientResetCount = ProtocolUtils.getIntLE(data, 168, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 170 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.CLEAR_DATA, "Transient reset");
            events.add(me);
        }

        meterErrorCount = ProtocolUtils.getIntLE(data, 182, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 184 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.FATAL_ERROR, "Meter error");
            events.add(me);
        }

        terminalCoverCount = ProtocolUtils.getIntLE(data, 196, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 198 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.TERMINAL_OPENED, "Terminal Cover Cumulative Event Count");
            events.add(me);
        }

        mainCoverCount = ProtocolUtils.getIntLE(data, 210, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 212 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.COVER_OPENED, "Main Cover Cumulative Event Count");
            events.add(me);
        }

        internalBatteryCount = ProtocolUtils.getIntLE(data, 224, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset = 226 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.OTHER, "Internal Battery");
            events.add(me);
        }

        externalBatteryCount = ProtocolUtils.getIntLE(data, 238, 2);
        for( int i = 0; i < HISTORY_SIZE; i ++ ) {
            int offset =240 + (i*4);
            shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0xFFFFFFFFL;
            if( shift == 0 ) continue;
            date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
            MeterEvent me = new MeterEvent(date, MeterEvent.OTHER, "External Battery");
            events.add(me);
        }

    }

    public String toString() {

        StringBuffer rslt = new StringBuffer()
        .append( "HistoricalEventRegister[ \n" )
        .append( " reverseRunCount=" + reverseRunCount + "\n" )
        .append( " phaseFailureCount=" + phaseFailureCount + "\n" )
        .append( " powerFailCount=" + powerFailCount + "\n" )
        .append( " programmingCount=" + programmingCount + "\n" )
        .append( " ctRatioCount=" + ctRatioCount + "\n" )
        .append( " billingCount=" + billingCount + "\n" )
        .append( " transientResetCount=" + transientResetCount + "\n" )
        .append( " meterErrorCount=" + meterErrorCount + "\n" )
        .append( " terminalCoverCount=" + terminalCoverCount + "\n" )
        .append( " mainCoverCount=" + mainCoverCount + "\n" )
        .append( " internalBatteryCount=" + internalBatteryCount + "\n" )
        .append( " externalBatteryCount=" + externalBatteryCount + "\n" );

        rslt.append( "\n" );

        Iterator i = events.iterator();
        while( i.hasNext() ) {
            MeterEvent me = (MeterEvent)i.next();
            String msg = "MeterEvent [ "
                    + me.getTime() + ", "
                    + me.getMessage() + "] ";
            rslt.append( " " + msg + "\n" );
        }

        rslt.append( "]\n" ).toString();

        return rslt.toString();
    }

    public int getReverseRunCount() {
        return reverseRunCount;
    }

    public int getPhaseFailureCount() {
        return phaseFailureCount;
    }

    public int getPowerFailCount() {
        return powerFailCount;
    }

    public int getProgrammingCount() {
        return programmingCount;
    }

    public int getCtRatioCount() {
        return ctRatioCount;
    }

    public int getBillingCount() {
        return billingCount;
    }

    public int getTransientResetCount() {
        return transientResetCount;
    }

    public int getMeterErrorCount() {
        return meterErrorCount;
    }

    public int getTerminalCoverCount() {
        return terminalCoverCount;
    }

    public int getMainCoverCount() {
        return mainCoverCount;
    }

    public int getInternalBatteryCount() {
        return internalBatteryCount;
    }

    public int getExternalBatteryCount() {
        return externalBatteryCount;
    }
}
