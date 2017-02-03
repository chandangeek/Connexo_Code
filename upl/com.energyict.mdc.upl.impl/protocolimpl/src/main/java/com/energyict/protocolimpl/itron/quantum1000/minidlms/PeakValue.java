/*
 * PeakValue.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class PeakValue {
    
    private float value; //FLOAT,                           D=6, E=128..137 PEAK MAX & MIN
    private Date timeOfOccurrence; // EXTENDED_DATE_TIME,
    private float coin1Value; // FLOAT,                     D=133, E=128..137 PEAK MAX & MIN
    private float coin2Value; // FLOAT,                     D=134, E=128..137 PEAK MAX & MIN
    private float coin3Value; // FLOAT,                     D=135, E=128..137 PEAK MAX & MIN
    
    /**
     * Creates a new instance of PeakValue
     */
    public PeakValue(byte[] data,int offset, TimeZone timeZone) throws IOException {
        setValue(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setTimeOfOccurrence(Utils.getDateFromDateTimeExtended(data,offset, timeZone));
        offset+=Utils.getDateTimeExtendedSize();
        setCoin1Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setCoin2Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setCoin3Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PeakValue:\n");
        strBuff.append("   coin1Value="+getCoin1Value()+"\n");
        strBuff.append("   coin2Value="+getCoin2Value()+"\n");
        strBuff.append("   coin3Value="+getCoin3Value()+"\n");
        strBuff.append("   timeOfOccurrence="+getTimeOfOccurrence()+"\n");
        strBuff.append("   value="+getValue()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 24;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Date getTimeOfOccurrence() {
        return timeOfOccurrence;
    }

    public void setTimeOfOccurrence(Date timeOfOccurrence) {
        this.timeOfOccurrence = timeOfOccurrence;
    }

    public float getCoin1Value() {
        return coin1Value;
    }

    public void setCoin1Value(float coin1Value) {
        this.coin1Value = coin1Value;
    }

    public float getCoin2Value() {
        return coin2Value;
    }

    public void setCoin2Value(float coin2Value) {
        this.coin2Value = coin2Value;
    }

    public float getCoin3Value() {
        return coin3Value;
    }

    public void setCoin3Value(float coin3Value) {
        this.coin3Value = coin3Value;
    }
    
    
    
}
