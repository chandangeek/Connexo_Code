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
public class DataRecordTypeI extends AbstractDataRecordType {
    
    private Calendar calendar;
    TimeZone timeZone;
    private boolean inValid;
    private boolean summerTime;
    private boolean leapYear;
    private int dstShiftInHours;
    
    
    /**
     * Creates a new instance of DataRecordTypeF_CP32 
     */
    public DataRecordTypeI(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
//        public DataRecordTypeF_CP32() {
//        }
//        public static void main(String[] args) {
//            System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new DataRecordTypeF_CP32()));
//        }         
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataRecordTypeI");
        strBuff.append("   calendar="+getCalendar().getTime()+"\n");
        strBuff.append("   inValid="+isInValid()+"\n");
        strBuff.append("   summerTime="+isSummerTime()+"\n");
        strBuff.append("   leapYear="+isLeapYear()+"\n");
        strBuff.append("   dstShiftInHours="+getDstShiftInHours()+"\n");
        
        return strBuff.toString();
    }  
    
    protected void doParse(byte[] data) throws IOException {
        setCalendar(Calendar.getInstance(timeZone));
        getCalendar().set(Calendar.SECOND,ProtocolUtils.getInt(data,0,1)&0x3F);
        getCalendar().set(Calendar.MINUTE,ProtocolUtils.getInt(data,1,1)&0x3F);
        getCalendar().set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,2,1)&0x1F);
        getCalendar().set(Calendar.DATE,ProtocolUtils.getInt(data,3,1)&0x1F);
        getCalendar().set(Calendar.MONTH,(ProtocolUtils.getInt(data,4,1)&0x0F)-1);
        getCalendar().set(Calendar.YEAR,2000 + (((ProtocolUtils.getInt(data,4,1)&0xf0)>>1)|((ProtocolUtils.getInt(data,3,1)&0xe0)>>5)));
        
        setInValid((ProtocolUtils.getInt(data,1,1)&0x80)==0x80 ? true : false);
        setSummerTime((ProtocolUtils.getInt(data,0,1)&0x40)==0x40 ? true : false);
        
        setLeapYear((ProtocolUtils.getInt(data,0,1)&0x80)==0x80);
        int temp = (ProtocolUtils.getInt(data,1,1)&0x40)==0x40 ? 1:-1;
        setDstShiftInHours(temp * ((ProtocolUtils.getInt(data,5,1)&0xC0)>>6));
    }
    
    static public void main(String[] args) {

        try {
            DataRecordTypeF_CP32 d = new DataRecordTypeF_CP32(TimeZone.getDefault());
            byte[] data = new byte[]{0x22,0x0b,(byte)0xe3,0x0a};
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

    public boolean isInValid() {
        return inValid;
    }

    public void setInValid(boolean inValid) {
        this.inValid = inValid;
    }

    public boolean isSummerTime() {
        return summerTime;
    }

    public void setSummerTime(boolean summerTime) {
        this.summerTime = summerTime;
    }

    public boolean isLeapYear() {
        return leapYear;
    }

    public void setLeapYear(boolean leapYear) {
        this.leapYear = leapYear;
    }

    public int getDstShiftInHours() {
        return dstShiftInHours;
    }

    public void setDstShiftInHours(int dstShiftInHours) {
        this.dstShiftInHours = dstShiftInHours;
    }
}
