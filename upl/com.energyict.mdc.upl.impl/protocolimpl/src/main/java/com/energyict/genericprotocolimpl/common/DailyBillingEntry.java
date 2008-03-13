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
import java.math.*;
import java.util.*;

/**
 *
 * @author kvds
 */
public class DailyBillingEntry {
    
    private Calendar calendar;
    private int status;
    private BigDecimal[] values;
    
    /** Creates a new instance of DailyBillingEntry */
    public DailyBillingEntry(Structure structure,TimeZone timeZone) throws IOException {
        int offset=0;
        if (structure.getDataType(offset) instanceof NullData)
            setCalendar(null);
        else
            setCalendar((new DateTime(structure.getDataType(offset).getBEREncodedByteArray(),0,timeZone)).getValue());
        offset++;
        if (structure.getDataType(offset).isUnsigned8()) {
            setStatus(structure.getDataType(offset).intValue());
            offset++;
        }
        values = new BigDecimal[(structure.nrOfDataTypes()-offset)];
        for (int index=0;index<values.length;index++) {
           values[index] = structure.getDataType(offset+index).toBigDecimal();
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DailyBillingEntry:\n");
        strBuff.append("   calendar="+getCalendar().getTime()+"\n");
        strBuff.append("   status="+getStatus()+"\n");
        for (int index=0;index<values.length;index++) {
           strBuff.append("   values["+index+"]="+getValues()[index]+"\n");    
        }
        return strBuff.toString();
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public BigDecimal[] getValues() {
        return values;
    }

    
}
