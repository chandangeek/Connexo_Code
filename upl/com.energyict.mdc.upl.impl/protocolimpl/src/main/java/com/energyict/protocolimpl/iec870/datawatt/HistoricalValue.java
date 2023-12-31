/*
 * HistoricalValues.java
 *
 * Created on 14 juli 2003, 13:54
 */

package com.energyict.protocolimpl.iec870.datawatt;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec870.*;

/**
 *
 * @author  Koen
 */
public class HistoricalValue implements Comparable {
    
    int status;
    BigDecimal value;
    Date date;
    Channel channel;
    boolean dateInvalid;
    
    /** Creates a new instance of HistoricalValues */
    public HistoricalValue(int status, BigDecimal value, Date date, Channel channel, boolean dateInvalid) {
        this.date=date;
        this.value=value;
        this.channel=channel;
        this.status=status;
        this.dateInvalid=dateInvalid;
    }
    
    public boolean isDateInValid() {
        return dateInvalid;
    }
    
    public Date getDate() {
        return date;
    }
    public int getStatus() {
        return status;
    }
    public BigDecimal getValue() {
        return value;
    }
    public Channel getChannel() {
        return channel;
    }
    
    // for all types, bit7 of status means Invalid (IV)
    public int getEIStatus() {
        int eiStatus=0;
        if (getChannel().isCounterInput()) {
            if ((getStatus() & 0x80) == 0x80) {    // IV
                eiStatus |= IntervalData.CORRUPTED;
            }
            if ((getStatus() & 0x20) == 0x20) {    // CY
                eiStatus |= IntervalData.OVERFLOW;
            }
        }
        else if (getChannel().isAnalogInput() || getChannel().isAnalogOutput()) {
            if ((getStatus() & 0x80) == 0x80) {    // IV
                eiStatus |= IntervalData.CORRUPTED;
            }
            if ((getStatus() & 0x01) == 0x01) {    // OV
                eiStatus |= IntervalData.OVERFLOW;
            }
        }
        else if (getChannel().isDigitalInput()) {
            if ((getStatus() & 0x80) == 0x80) {    // IV
                eiStatus |= IntervalData.CORRUPTED;
            }
        }
        
        if (isDateInValid()) {
            eiStatus |= IntervalData.SHORTLONG;
        }
        
        return eiStatus;
    }
    
    public int compareTo(Object o) {
        return (date.compareTo(((HistoricalValue)o).getDate()));
    }
    
    public String toString() {
        if (date != null)
            return "channel="+channel.getChannelId()+" "+getDate()+" "+isDateInValid()+" val="+value.toString()+" status="+status+" addresstype=0x"+Integer.toHexString(channel.getChannelType());
        else
            return "channel="+channel.getChannelId()+" val="+value.toString()+" status="+status+" addresstype=0x"+Integer.toHexString(channel.getChannelType());
    }
    
}
