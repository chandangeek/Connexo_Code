/*
 * LogbookEntry.java
 *
 * Created on 5 december 2007, 16:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.common;

import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.mdc.protocol.device.events.MeterEvent;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class LogbookEntry {

    private Date date;
    private LogbookEvent event;

    /** Creates a new instance of LogbookEntry */
    public LogbookEntry(Structure structure,TimeZone timeZone) throws IOException {

        if (structure.getDataType(0) instanceof NullData)
            setDate(null);
        else
            setDate((new DateTime(structure.getDataType(0).getBEREncodedByteArray(),0,timeZone)).getValue().getTime());


        setEvent(LogbookEvent.findLogbookEvent(structure.getDataType(1).intValue()));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LogbookEntry:\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   event="+getEvent()+"\n");
        return strBuff.toString();
    }

    public Date getDate() {
        return date;
    }

    private void setDate(Date date) {
        this.date = date;
    }

    public LogbookEvent getEvent() {
        return event;
    }

    private void setEvent(LogbookEvent event) {
        this.event = event;
    }

    public MeterEvent toMeterEvent() {
        return event.meterEvent(getDate());
    }

}