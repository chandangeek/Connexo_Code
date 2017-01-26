/*
 * ClockDefinition.java
 *
 * Created on 8 juli 2004, 17:17
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class ClockDefinition extends AbstractLogicalAddress {
    
    private static final int NR_OF_TIMESTAMPS=5;
    
    Date[] dstAdvanceTimestamps = new Date[NR_OF_TIMESTAMPS];
    Date[] dstRetardTimestamps = new Date[NR_OF_TIMESTAMPS];
    int amountOfAdvance; // 0=1 hour, 1=2 hour
                         // bit0..4 dst 1..5
    int amountOfRetard; // 0=1 hour, 1=2 hour
                        // bit0..4 dst 1..5
    int daylightSaving; // 0=disabled, 1=programmed, 2=European
    int clockSyncSource; // 0=crystal, 1=mains
    
    
    /** Creates a new instance of ClockDefinition */
    public ClockDefinition(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ClockDefinition:");
        
        strBuff.append("DSTAdvanceTimestamps: ");
        for(int i=0;i<NR_OF_TIMESTAMPS;i++) {
            if (i!=0) strBuff.append(", ");
            strBuff.append(i+"="+getDstAdvanceTimestamp(i).toString());
        }
        strBuff.append(" ");
        strBuff.append("DSTRetardTimestamps: "); 
        for(int i=0;i<NR_OF_TIMESTAMPS;i++) {
            if (i!=0) strBuff.append(", ");
            strBuff.append(i+"="+getDstRetardTimestamp(i).toString());
        }
        strBuff.append(", amoundOfAdvance="+getAmountOfAdvance());
        strBuff.append(", amountOfRetard="+getAmountOfRetard());
        strBuff.append(", daylightSaving="+getDaylightSaving());
        strBuff.append(", clockSyncSource="+getClockSyncSource());
        
        
        
        return strBuff.toString();
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        
        // KV TO_DO following the protocol description data should be read in little indian. The protocol analysis shows different...
        for(int i=0;i<NR_OF_TIMESTAMPS;i++) {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTimeInMillis(ProtocolUtils.getLong(data,i*4,4)*1000);
            dstAdvanceTimestamps[i] = calendar.getTime();
        }
        for(int i=0;i<NR_OF_TIMESTAMPS;i++) {
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTimeInMillis(ProtocolUtils.getLong(data,NR_OF_TIMESTAMPS*4+i*4,4)*1000);
            dstRetardTimestamps[i] = calendar.getTime();
        }
        setAmountOfAdvance(ProtocolUtils.getInt(data,NR_OF_TIMESTAMPS*2*4,1));
        setAmountOfRetard(ProtocolUtils.getInt(data,NR_OF_TIMESTAMPS*2*4+1,1));
        setDaylightSaving(ProtocolUtils.getInt(data,NR_OF_TIMESTAMPS*2*4+2,1));
        setClockSyncSource(ProtocolUtils.getInt(data,NR_OF_TIMESTAMPS*2*4+3,1));
        
    }
    
    /**
     * Getter for property dstAdvanceTimestamps.
     * @return Value of property dstAdvanceTimestamps.
     */
    public java.util.Date[] getDstAdvanceTimestamps() {
        return this.dstAdvanceTimestamps;
    }

    public java.util.Date getDstAdvanceTimestamp(int index) {
        return this.dstAdvanceTimestamps[index];
    }
    
    /**
     * Setter for property dstAdvanceTimestamps.
     * @param dstAdvanceTimestamps New value of property dstAdvanceTimestamps.
     */
    public void setDstAdvanceTimestamps(java.util.Date[] dstAdvanceTimestamps) {
        this.dstAdvanceTimestamps = dstAdvanceTimestamps;
    }
    
    /**
     * Getter for property dstRetardTimestamps.
     * @return Value of property dstRetardTimestamps.
     */
    public java.util.Date[] getDstRetardTimestamps() {
        return this.dstRetardTimestamps;
    }
    
    public java.util.Date getDstRetardTimestamp(int index) {
        return this.dstRetardTimestamps[index];
    }
    
    /**
     * Setter for property dstRetardTimestamps.
     * @param dstRetardTimestamps New value of property dstRetardTimestamps.
     */
    public void setDstRetardTimestamps(java.util.Date[] dstRetardTimestamps) {
        this.dstRetardTimestamps = dstRetardTimestamps;
    }
    
    /**
     * Getter for property amountOfAdvance.
     * @return Value of property amountOfAdvance.
     */
    public int getAmountOfAdvance() {
        return amountOfAdvance;
    }
    
    /**
     * Setter for property amountOfAdvance.
     * @param amountOfAdvance New value of property amountOfAdvance.
     */
    public void setAmountOfAdvance(int amountOfAdvance) {
        this.amountOfAdvance = amountOfAdvance;
    }
    
    /**
     * Getter for property amountOfRetard.
     * @return Value of property amountOfRetard.
     */
    public int getAmountOfRetard() {
        return amountOfRetard;
    }
    
    /**
     * Setter for property amountOfRetard.
     * @param amountOfRetard New value of property amountOfRetard.
     */
    public void setAmountOfRetard(int amountOfRetard) {
        this.amountOfRetard = amountOfRetard;
    }
    
    /**
     * Getter for property daylightSaving.
     * @return Value of property daylightSaving.
     */
    public int getDaylightSaving() {
        return daylightSaving;
    }
    
    /**
     * Setter for property daylightSaving.
     * @param daylightSaving New value of property daylightSaving.
     */
    public void setDaylightSaving(int daylightSaving) {
        this.daylightSaving = daylightSaving;
    }
    
    /**
     * Getter for property clockSyncSource.
     * @return Value of property clockSyncSource.
     */
    public int getClockSyncSource() {
        return clockSyncSource;
    }
    
    /**
     * Setter for property clockSyncSource.
     * @param clockSyncSource New value of property clockSyncSource.
     */
    public void setClockSyncSource(int clockSyncSource) {
        this.clockSyncSource = clockSyncSource;
    }
    
}
