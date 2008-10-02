/*
 * Clock.java
 *
 * Created on 19 augustus 2004, 9:28
 */

package com.energyict.dlms.cosem;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class Clock extends AbstractCosemObject {
    public final int DEBUG=0;
    static public final int CLASSID=8;
    
    Date dateTime;
    boolean dateTimeCached=false;
    int timeZone=-1;
    int status;
    Date dsDateTimeBegin=null;
    Date dsDateTimeEnd=null;
    int dsDeviation=-1;
    int dsEnabled=-1;
    int dstFlag=-1;
    
    static final byte[] LN=new byte[]{0,0,1,0,0,(byte)255};
    
    /** Creates a new instance of Clock */
    public Clock(ProtocolLink protocolLink) {
        super(protocolLink,new ObjectReference(LN));
    }
    public Clock(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}    
    
    public void setDateTime(OctetString octetString) throws IOException {
        byte[] data = {0x09,0x0C};
        dateTime = getDateTime(ProtocolUtils.concatByteArrays(data,octetString.getArray()));
        dateTimeCached=true;        
    }
    
    /**
     * Getter for property dateTime.
     * Apply roundTripCorrection
     * @return Value of property dateTime.
     */
    public java.util.Date getDateTime() throws IOException {
        if (dateTimeCached)
            return dateTime;
        else {
            byte[] responseData;
            responseData = getResponseData(TIME_TIME);
            if (DEBUG == 1) ProtocolUtils.printResponseData(responseData);
            return getDateTime(responseData,protocolLink.getRoundTripCorrection());
        }
    }
    
    /**
     * Getter for property dateTime. Method used when attribute of Clock object 
     * taken from profile buffer data!
     * Do not apply roundTripCorrection!
     * @param data to parse
     * @return Value of property dateTime.
     */
    public java.util.Date getDateTime(byte[] responseData) throws IOException {
        return getDateTime(responseData,-1);
    }
    
    private java.util.Date getDateTime(byte[] responseData,int roundtripCorrection) throws IOException {
        Calendar gcalendarMeter=null;
        
        if ((responseData[13]&(byte)0x80)==(byte)0x80)
            dstFlag = 1;
        else
            dstFlag = 0;
        
        
        gcalendarMeter = buildCalendar(responseData);
        
        if (roundtripCorrection != -1)
           dateTime = new Date(gcalendarMeter.getTime().getTime()-(long)roundtripCorrection);
        else
           dateTime = new Date(gcalendarMeter.getTime().getTime());
        return dateTime;
    }
    
    private Calendar buildCalendar(byte[] responseData) throws IOException {
        Calendar gcalendarMeter=null;
        
        int status = (int)responseData[13] & 0xFF;
        if (status != 0xFF) {
            if (protocolLink.isRequestTimeZone()) {
                int reqTimeZone = getTimeZone();
                gcalendarMeter = ProtocolUtils.getCalendar((responseData[13]&(byte)0x80)==(byte)0x80,reqTimeZone);
            } else
                gcalendarMeter = ProtocolUtils.initCalendar((responseData[13]&(byte)0x80)==(byte)0x80,protocolLink.getTimeZone());
        }
        else {
            gcalendarMeter = ProtocolUtils.getCleanCalendar(protocolLink.getTimeZone());
        }
        
        int year = (int)ProtocolUtils.getShort(responseData,2)&0x0000FFFF;
        if (year != 0xFFFF) gcalendarMeter.set(gcalendarMeter.YEAR,year);
        
        int month = (int)responseData[4]&0xFF;
        if (month != 0xFF) gcalendarMeter.set(gcalendarMeter.MONTH,month-1);
        
        int date = (int)responseData[5]&0xFF;
        if (date != 0xFF) gcalendarMeter.set(gcalendarMeter.DAY_OF_MONTH,date);
        
        int hour = (int)responseData[7]&0xFF;
        if (hour != 0xFF) gcalendarMeter.set(gcalendarMeter.HOUR_OF_DAY,hour);
        
        int minute = (int)responseData[8]&0xFF;
        if (minute != 0xFF) gcalendarMeter.set(gcalendarMeter.MINUTE,minute);
        
        int seconds = (int)responseData[9]&0xFF;
        if (seconds != 0xFF) gcalendarMeter.set(gcalendarMeter.SECOND,seconds);
        
        gcalendarMeter.set(gcalendarMeter.MILLISECOND,0);
        
        return gcalendarMeter;
    }
    
    /**
     * Getter for property timeZone.
     * @return Value of property timeZone.
     */
    public int getTimeZone() throws IOException {
        if (timeZone == -1) {
            timeZone = (int)getLongData(TIME_TIME_ZONE)*(-1)/60;
        }
        return timeZone;
    }
    
    
    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus() throws IOException {
        status = (int)getLongData(TIME_STATUS);
        return status;
    }
    
    
    /**
     * Getter for property dsDateTimeBegin.
     * @return Value of property dsDateTimeBegin.
     */
    public java.util.Date getDsDateTimeBegin() throws IOException {
        if (dsDateTimeBegin == null) {
            byte[] responseData = getResponseData(TIME_DS_BEGIN);
            dsDateTimeBegin = buildCalendar(responseData).getTime();
        }
        return dsDateTimeBegin;
    }
    
    
    /**
     * Getter for property dsDateTimeEnd.
     * @return Value of property dsDateTimeEnd.
     */
    public java.util.Date getDsDateTimeEnd() throws IOException {
        if (dsDateTimeEnd == null) {
            byte[] responseData = getResponseData(TIME_DS_END);
            dsDateTimeEnd = buildCalendar(responseData).getTime();
        }
        return dsDateTimeEnd;
    }
    
    /**
     * Getter for property dsDeviation.
     * @return Value of property dsDeviation.
     */
    public int getDsDeviation() throws IOException {
        if (dsDeviation == -1) {
            dsDeviation = (int)getLongData(TIME_DS_DEVIATION);
        }
        return dsDeviation;
    }
    
    /**
     * Getter for property dsEnabled.
     * @return Value of property dsEnabled.
     */
    public boolean isDsEnabled() throws IOException {
        if (dsEnabled == -1) {
            dsEnabled = (int)getLongData(TIME_DAYLIGHT_SAVING);
        }
        return dsEnabled==1;
    }
    
    public int getDstFlag() throws IOException {
        if (dstFlag == -1)
            throw new IOException("Clock, getDstFlag, dstFlag not evaluated. getDateTime() should invoked first.");
        return dstFlag;
    }
    
    protected int getClassId() {
        return CLASSID;
    }

    public void setTimeAttr(DateTime dateTime) throws IOException {
        write(2, dateTime.getBEREncodedByteArray());
    }
    
    public AbstractDataType getTimeAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2));
    }
    
}
