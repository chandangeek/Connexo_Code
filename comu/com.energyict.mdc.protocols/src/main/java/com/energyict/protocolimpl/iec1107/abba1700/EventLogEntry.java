/*
 * EventLogEntry.java
 *
 * Created on 15 juni 2004, 9:02
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import java.util.Date;
/**
 *
 * @author  Koen
 */
public class EventLogEntry {

    static public final int PHASE_1_FAILURE=1;
    static public final int PHASE_2_FAILURE=2;
    static public final int PHASE_3_FAILURE=3;
    static public final int POWER_FAILURE=4;
    static public final int REVERSE_RUN=5;
    static public final int PHASE_1_OVERCURRENT=6;
    static public final int PHASE_2_OVERCURRENT=7;
    static public final int PHASE_3_OVERCURRENT=8;
    String[] values={"phase 1 failure","phase 2 failure","phase 3 failure","power failure","reverse run","phase 1 overcurrent","phase 2 overcurrent","phase 3 overcurrent"};

    Date dateTime;
    int value;

    /** Creates a new instance of EventLogEntry */
    public EventLogEntry(Date dateTime, int value) {
        this.dateTime=dateTime;
        this.value=value;
    }

    public String toString() {
        return "dateTime="+getDateTime()+", value="+values[getValue()-1]+"\n";
    }
    /**
     * Getter for property dateTime.
     * @return Value of property dateTime.
     */
    public java.util.Date getDateTime() {
        return dateTime;
    }

    /**
     * Setter for property dateTime.
     * @param dateTime New value of property dateTime.
     */
    public void setDateTime(java.util.Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(int value) {
        this.value = value;
    }

}
