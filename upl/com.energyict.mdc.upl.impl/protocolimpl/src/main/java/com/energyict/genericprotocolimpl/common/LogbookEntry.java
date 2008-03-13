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

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.protocol.*;
import java.io.*;
import java.util.*;

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
    
    static public void main(String[] args) {
        try {
            byte[] data = new byte[]{(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD7,(byte)0x0C,(byte)0x04,(byte)0x02,(byte)0x17,(byte)0x2F,(byte)0x35,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x08,(byte)0x16,(byte)0x14,(byte)0x02,(byte)0x02,(byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD7,(byte)0x0C,(byte)0x05,(byte)0x03,(byte)0x06,(byte)0x2D,(byte)0x18,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x08,(byte)0x16,(byte)0xB6};
            Array a = AXDRDecoder.decode(data).getArray();
            for (int index=0;index<a.nrOfDataTypes();index++) {
                LogbookEntry l = new LogbookEntry(a.getDataType(index).getStructure(), TimeZone.getDefault());
                System.out.println(l);
            }
        }
        catch(IOException e) {
           e.printStackTrace();
        }
    }
    
}
