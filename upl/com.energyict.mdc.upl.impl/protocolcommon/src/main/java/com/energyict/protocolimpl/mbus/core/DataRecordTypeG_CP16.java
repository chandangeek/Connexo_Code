/*
 * DataRecordTypeF_CP32.java
 *
 * Created on 3 oktober 2007, 11:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class DataRecordTypeG_CP16 extends AbstractDataRecordType {
    
    private Calendar calendar;
    TimeZone timeZone;
    
    /**
     * Creates a new instance of DataRecordTypeF_CP32 
     */
    public DataRecordTypeG_CP16(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataRecordTypeG_CP16:\n");
        strBuff.append("   calendar="+getCalendar().getTime()+"\n");
        return strBuff.toString();
    }  
    
    protected void doParse(byte[] data) throws IOException {
        setCalendar(Calendar.getInstance(timeZone));
        getCalendar().set(Calendar.DATE,ProtocolUtils.getInt(data,0,1)&0x1F);
        getCalendar().set(Calendar.MONTH,(ProtocolUtils.getInt(data,1,1)&0x0F)-1);
        getCalendar().set(Calendar.YEAR,2000 + (((ProtocolUtils.getInt(data,1,1)&0xf0)>>1)|((ProtocolUtils.getInt(data,0,1)&0xe0)>>5)));
    }
    
    static public void main(String[] args) {

        try {
            DataRecordTypeG_CP16 d = new DataRecordTypeG_CP16(TimeZone.getDefault());
            byte[] data = new byte[]{(byte)0x00,0x00};
            d.parse(data);
            System.out.println(d);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
