/*
 * CP56Time2A.java
 *
 * Created on 1 juli 2003, 9:02
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
public class CP56Time2a {
    private static final int LENGTH=7;
    private static final int DAY_OF_MONTH=0x1F;
    private static final int HOURS=0x1F;
    private static final int MINUTES=0x3F;

    Calendar calendar=null;
    TimeZone timeZone=null;
    boolean inValid=false;
    byte[] data=null;

    /** Creates a new instance of CP56Time2A */
    public CP56Time2a(TimeZone timeZone,byte[] data,int offset) throws IEC870TypeException {
        this.timeZone=timeZone;
        this.data=data;
        if ((data.length - offset) < LENGTH)
            throw new IEC870TypeException("CP56Time2A, length "+(data.length - offset)+" < "+LENGTH);
        else
            parse(ProtocolUtils.getSubArray(data,offset,(offset+LENGTH)-1));
    }

    /** Creates a new instance of CP56Time2A */
    public CP56Time2a(Calendar calendar) throws IEC870TypeException {
        this.calendar = calendar;
    }

    public Date getDate() {
        return calendar.getTime();
    }
    public Calendar getCalendar() {
        return calendar;
    }
    public byte[] getData() {
        buildData();
        return data;
    }

    public boolean isInValid() {
        return inValid;
    }

    private void parse(byte[] data) throws IEC870TypeException {
        try {
            calendar = Calendar.getInstance(timeZone);
            calendar.clear();
            calendar.set(Calendar.YEAR,ProtocolUtils.getInt(data,6,1)+2000);
            calendar.set(Calendar.MONTH,ProtocolUtils.getInt(data,5,1)-1);
            calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data,4,1)&DAY_OF_MONTH);
            calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,3,1)&HOURS);
//            if (timeZone.useDaylightTime() != ((ProtocolUtils.getInt(data,3,1) & 0x80) == 0x80))
//                throw new IEC870TypeException("CP56Time2A, parse, difference in DST use, change devicetimezone!");
            calendar.set(Calendar.MINUTE,ProtocolUtils.getInt(data,2,1)&MINUTES);
            inValid = (ProtocolUtils.getInt(data,2,1)&0x80)==0x80 ? true : false;
            calendar.set(Calendar.MILLISECOND,ProtocolUtils.getIntLE(data,0,2));
        }
        catch(IOException e) {
            throw new IEC870TypeException("CP56Time2A, parse, IOException, "+e.getMessage());
        }
    }

    public String toString() {
        return calendar.getTime().toString();
    }

    private void buildData() {
        data = new byte[LENGTH];
        data[6] = (byte)(calendar.get(Calendar.YEAR)-2000);
        data[5] = (byte)(calendar.get(Calendar.MONTH)+1);
        data[4] = (byte)(calendar.get(Calendar.DAY_OF_WEEK) << 5);
        data[4] |= (byte)calendar.get(Calendar.DAY_OF_MONTH);
        data[3] = (byte)calendar.get(Calendar.HOUR_OF_DAY);
        if (calendar.getTimeZone().useDaylightTime())
            if (calendar.getTimeZone().inDaylightTime(calendar.getTime()))
                data[3] |= (byte)0x80;
        data[2] = (byte)calendar.get(Calendar.MINUTE);
        int ms = calendar.get(Calendar.SECOND) * 1000;
        data[1] = (byte)((ms>>8)&0xFF);
        data[0] = (byte)(ms&0xFF);
    }

} // public class CP56Time2a
