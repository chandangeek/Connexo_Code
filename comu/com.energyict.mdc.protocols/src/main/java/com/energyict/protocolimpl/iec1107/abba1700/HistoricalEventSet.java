/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HistoricalEventSet.java
 *
 * Created on 15 juni 2004, 9:21
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * @author  Koen
 */
public class HistoricalEventSet {

    int phaseFailureCount;
    int powerFailureCount;
    int phaseOverCurrentCount;
    int reverseRunCount;
    int remainingBattSupportTime;

    List eventLogEntries=new ArrayList();
    TimeZone timeZone;

    /**
     * Creates a new instance of HistoricalEventSet
     */
    public HistoricalEventSet(byte[] data, TimeZone timeZone) throws IOException {
        this.timeZone=timeZone;
        parse(data);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("phaseFailureCount="+getPhaseFailureCount()+"\n");
        strBuff.append("powerFailureCount="+getPowerFailureCount()+"\n");
        strBuff.append("phaseOverCurrentCount="+getPhaseOverCurrentCount()+"\n");
        strBuff.append("reverseRunCount="+getReverseRunCount()+"\n");
        strBuff.append("remainingBattSupporttime="+ getRemainingBattSupportTime()+"\n");
        Iterator it = eventLogEntries.iterator();
        while(it.hasNext()) {
            EventLogEntry ele = (EventLogEntry)it.next();
            strBuff.append(ele.toString());
        }
        return strBuff.toString();
    }

    private void parse(byte[] data) throws IOException {
        long shift;
        Date date;

        // Phase failure
        setPhaseFailureCount(ProtocolUtils.getIntLE(data,0,2));
        shift = (long)ProtocolUtils.getIntLE(data,2,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,ProtocolUtils.getIntLE(data,14,1)));
        shift = (long)ProtocolUtils.getIntLE(data,6,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,ProtocolUtils.getIntLE(data,15,1)));
        shift = (long)ProtocolUtils.getIntLE(data,10,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,ProtocolUtils.getIntLE(data,16,1)));

        // Power failure
        setPowerFailureCount(ProtocolUtils.getIntLE(data,17,2));
        shift = (long)ProtocolUtils.getIntLE(data,19,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.POWER_FAILURE));
        shift = (long)ProtocolUtils.getIntLE(data,23,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.POWER_FAILURE));
        shift = (long)ProtocolUtils.getIntLE(data,27,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.POWER_FAILURE));

        // Reverse Run
        setReverseRunCount(ProtocolUtils.getIntLE(data,31,2));
        shift = (long)ProtocolUtils.getIntLE(data,33,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.REVERSE_RUN));
        shift = (long)ProtocolUtils.getIntLE(data,37,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.REVERSE_RUN));
        shift = (long)ProtocolUtils.getIntLE(data,41,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.REVERSE_RUN));

        // Overcurrent phase failure
        setPhaseOverCurrentCount(ProtocolUtils.getIntLE(data,45,2));
        shift = (long)ProtocolUtils.getIntLE(data,47,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.PHASE_1_OVERCURRENT+ProtocolUtils.getIntLE(data,59,1)));
        shift = (long)ProtocolUtils.getIntLE(data,51,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.PHASE_1_OVERCURRENT+ProtocolUtils.getIntLE(data,60,1)));
        shift = (long)ProtocolUtils.getIntLE(data,55,4)&0xFFFFFFFFL;
        date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
        eventLogEntries.add(new EventLogEntry(date,EventLogEntry.PHASE_1_OVERCURRENT+ProtocolUtils.getIntLE(data,61,1)));

        // Battery support time
        setRemainingBattSupportTime(ProtocolUtils.getIntLE(data,62,4));
    }

    /**
     * Getter for property PhaseFailureCount.
     *
     * @return Value of property PhaseFailureCount.
     */
    public int getPhaseFailureCount() {
        return phaseFailureCount;
    }

    /**
     * Setter for property phaseFailureCount.
     *
     * @param phaseFailureCount New value of property phaseFailureCount.
     */
    public void setPhaseFailureCount(int phaseFailureCount) {
        this.phaseFailureCount = phaseFailureCount;
    }

    /**
     * Getter for property PowerFailureCount.
     *
     * @return Value of property powerFailureCount.
     */
    public int getPowerFailureCount() {
        return powerFailureCount;
    }

    /**
     * Setter for property powerFailureCount.
     *
     * @param powerFailureCount New value of property powerFailureCount.
     */
    public void setPowerFailureCount(int powerFailureCount) {
        this.powerFailureCount = powerFailureCount;
    }

    /**
     * Getter for property phaseOverCurrentCount.
     *
     * @return Value of property phaseOverCurrentCount.
     */
    public int getPhaseOverCurrentCount() {
        return phaseOverCurrentCount;
    }

    /**
     * Setter for property phaseOverCurrentCount.
     *
     * @param phaseOverCurrentCount New value of property phaseOverCurrentCount.
     */
    public void setPhaseOverCurrentCount(int phaseOverCurrentCount) {
        this.phaseOverCurrentCount = phaseOverCurrentCount;
    }

    /**
     * Getter for property reverseRunCount.
     *
     * @return Value of property reverseRunCount.
     */
    public int getReverseRunCount() {
        return reverseRunCount;
    }

    /**
     * Setter for property reverseRunCount.
     *
     * @param reverseRunCount New value of property reverseRunCount.
     */
    public void setReverseRunCount(int reverseRunCount) {
        this.reverseRunCount = reverseRunCount;
    }

    /**
     * Getter for property remainingBattSupporttime.
     *
     * @return Value of property remainingBattSupporttime.
     */
    public int getRemainingBattSupportTime() {
        return remainingBattSupportTime;
    }

    /**
     * Setter for property remainingBattSupporttime.
     *
     * @param remainingBattSupportTime New value of property remainingBattSupporttime.
     */
    public void setRemainingBattSupportTime(int remainingBattSupportTime) {
        this.remainingBattSupportTime = remainingBattSupportTime;
    }
}
