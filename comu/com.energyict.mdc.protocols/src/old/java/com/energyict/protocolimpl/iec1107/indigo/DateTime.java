/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DateTime.java
 *
 * Created on 7 juli 2004, 16:37
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
abstract public class DateTime extends AbstractLogicalAddress {

    abstract public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException;

    Date date;

    /** Creates a new instance of DateTime */
    public DateTime(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }


    public void parseDate(byte[] data, Calendar calendar) throws IOException {
        calendar.set(Calendar.YEAR,Integer.parseInt(new String(ProtocolUtils.getSubArray2(data,0,2)))+2000);
        calendar.set(Calendar.MONTH,Integer.parseInt(new String(ProtocolUtils.getSubArray2(data,2,2)))-1);
        calendar.set(Calendar.DAY_OF_MONTH,Integer.parseInt(new String(ProtocolUtils.getSubArray2(data,4,2))));
        calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(new String(ProtocolUtils.getSubArray2(data,6,2))));
        calendar.set(Calendar.MINUTE,Integer.parseInt(new String(ProtocolUtils.getSubArray2(data,8,2))));
        calendar.set(Calendar.SECOND,Integer.parseInt(new String(ProtocolUtils.getSubArray2(data,10,2))));
        setDate(calendar.getTime());
    }

    protected byte[] buildData(Calendar calendar) {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(ProtocolUtils.buildStringDecimal(calendar.get(Calendar.YEAR)-2000,2));
        strBuff.append(ProtocolUtils.buildStringDecimal(calendar.get(Calendar.MONTH)+1,2));
        strBuff.append(ProtocolUtils.buildStringDecimal(calendar.get(Calendar.DATE),2));
        strBuff.append(ProtocolUtils.buildStringDecimal(calendar.get(Calendar.HOUR_OF_DAY),2));
        strBuff.append(ProtocolUtils.buildStringDecimal(calendar.get(Calendar.MINUTE),2));
        strBuff.append(ProtocolUtils.buildStringDecimal(calendar.get(Calendar.SECOND),2));
        return ProtocolUtils.convertAscii2Binary(strBuff.toString().getBytes());
    }

    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date getDate() {
        return date;
    }

    /**
     * Setter for property date.
     * @param date New value of property date.
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }

}
