/*
 * CP24Time2a.java
 *
 * Created on 1 juli 2003, 10:30
 */

package com.energyict.protocolimpl.iec870;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class CP24Time2a {

    private static final int LENGTH=3;
    private static final int MINUTES=0x3F;

    Calendar calendar=null;
    boolean inValid=false;
    TimeZone timeZone=null;

    /** Creates a new instance of CP24Time2a */
    public CP24Time2a(TimeZone timeZone,byte[] data,int offset) throws IEC870TypeException {
        this.timeZone=timeZone;
        if ((data.length - offset) < LENGTH)
            throw new IEC870TypeException("CP24Time2a, length "+(data.length - offset)+" < "+LENGTH);
        else
            parse(ProtocolUtils.getSubArray(data,offset,(offset+LENGTH)-1));
    }

    public Date getDate() {
        return calendar.getTime();
    }
    public int getSeconds() {
        return calendar.get(Calendar.MINUTE)*60+calendar.get(Calendar.SECOND);
    }

    public boolean isInValid() {
        return inValid;
    }

    private void parse(byte[] data) throws IEC870TypeException {
        try {
            calendar = Calendar.getInstance(timeZone);
            calendar.clear();
            calendar.set(Calendar.MINUTE,ProtocolUtils.getInt(data,2,1)&MINUTES);
            inValid = (ProtocolUtils.getInt(data,2,1)&0x80)==0x80 ? true : false;
            calendar.set(Calendar.MILLISECOND,ProtocolUtils.getIntLE(data,0,2));
        }
        catch(IOException e) {
            throw new IEC870TypeException("CP24Time2a, parse, IOException, "+e.getMessage());
        }
    }

    public String toString() {
        return calendar.getTime().toString();
    }

}
