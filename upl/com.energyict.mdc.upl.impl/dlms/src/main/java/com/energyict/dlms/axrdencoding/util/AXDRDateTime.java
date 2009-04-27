package com.energyict.dlms.axrdencoding.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author gna
 * @since 03/02/2009
 * This is the copy of the DateTime class. 
 * The blue book describes the dateTime as an OctetString(size(12)). The 12 indicates that the length is fixed an does not have to be encode.
 * 
 * <pre>
 * 
 * deviation
 * 
 *  long:        in minutes of local time to GMT
 *               0x8000 = not specified
 * 
 * clock_status
 * 
 *  bit 0 (LSB): invalid value (Time could not be recovered after an incident.)
 *  bit 1:       doubtfull vlaue
 *  bit 2:       different clock base
 *  bit 3:       invalid clock status
 *  bit 4:       reserved
 *  bit 5:       reserved
 *  bit 6:       reserved
 *  bit 7 (MSB): daylist saving active
 *  
 * 
 * date_time 
 * 
 *  year highbyte;
 *  year lowbyte;
 *  month;
 *  day of month;
 *  day of week;
 *  hour;
 *  minute;
 *  second;
 *  hundredths of second;
 *  deviation highbyte;
 *  defiation lowbyte;
 *  clock status;
 * 
 * 
 * day of week is ignored: calendar knows this
 * deviation is ignored: protocol configuration provides timezone
 * 
 * </pre>
 *  
 */

public class AXDRDateTime extends AbstractDataType {

    private Calendar dateTime;
    private int status;
    
    public AXDRDateTime( ) {
    }

    public AXDRDateTime(TimeZone timeZone) {
    	dateTime = Calendar.getInstance(timeZone);
    }
    
    public AXDRDateTime(Calendar dateTime) {
    	this.dateTime = dateTime;
    }

    public AXDRDateTime(Date date) {
    	dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    	dateTime.setTime(date);
    }
    
    public AXDRDateTime(OctetString octetString) throws IOException {
    	this(octetString.getBEREncodedByteArray());
    }
    
    public AXDRDateTime(byte[] berEncodedData) throws IOException {
    	this(berEncodedData,0,TimeZone.getTimeZone("GMT"));
    }
    
    public AXDRDateTime(byte[] berEncodedData, int offset, TimeZone zone) throws IOException {
        
    	if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING){
            throw new IOException("AXDRDateTime, invalid identifier "+berEncodedData[offset]);
    	}
    	offset = offset + 2;

    	int tOffset = (short)ProtocolUtils.getInt(berEncodedData,11,2);
    	tOffset *= -1;	
    	int deviation = tOffset/60;
        TimeZone tz = TimeZone.getTimeZone("GMT"+(deviation<0?"":"+")+deviation);
//        dateTime.setTimeZone(tz);
    	dateTime = Calendar.getInstance(tz);
    	
    	System.out.println("1/ " + dateTime.getTime());
    	
        int year = ProtocolUtils.getShort(berEncodedData, offset );
        dateTime.set(Calendar.YEAR, year);
        offset = offset + 2;
        
        int month = ProtocolUtils.getByte2Int(berEncodedData, offset);
        dateTime.set(Calendar.MONTH, month-1);
        offset = offset + 1;
        
        int dayOfMonth = ProtocolUtils.getByte2Int(berEncodedData, offset);
        dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        offset = offset + 2; // one extra: skip day of week
        
        int hour = ProtocolUtils.getByte2Int(berEncodedData, offset);
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        offset = offset + 1; 
        
        int minute = ProtocolUtils.getByte2Int(berEncodedData, offset);
        dateTime.set(Calendar.MINUTE, minute);
        offset = offset + 1; 
        
        int second = ProtocolUtils.getByte2Int(berEncodedData, offset);
        dateTime.set(Calendar.SECOND, second);
        
        dateTime.set(Calendar.MILLISECOND, 0);
        
        System.out.println("2/ " + dateTime.getTime());
        
        System.out.println("3/ " + dateTime.getTime());
        
        offset = offset + 1; 
        
        offset = offset + 1; 
        
        offset = offset + 1;    // deviation highbyte
        
        offset = offset + 1;    // deviation lowbyte
        
        status = ProtocolUtils.getByte2Int(berEncodedData, offset);
        
        
    }

    public int getStatus() {
        return status;
    }

    protected byte[] doGetBEREncodedByteArray() throws IOException {
        
        Calendar v = getValue();
        
        int year        = v.get(Calendar.YEAR);
        int month       = v.get(Calendar.MONTH);
        int dayOfMonth  = v.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek   = v.get(Calendar.DAY_OF_WEEK);
        int hour        = v.get(Calendar.HOUR_OF_DAY);
        int minute      = v.get(Calendar.MINUTE);
        int second      = v.get(Calendar.SECOND);
        int hs          = v.get(Calendar.MILLISECOND) / 10;
        
//        int deviation = (v.getTimeZone().getRawOffset()/60000) + (v.getTimeZone().inDaylightTime(v.getTime())?1:0);
        
        return 
            new byte [] {   
                (byte) 0x09,
                (byte) 0x0c,	// fixed octetString, no need for giving the length
                (byte) ((year & 0Xff00 ) >> 8),
                (byte) (year & 0X00ff),
                (byte) (month + 1),
                (byte) (dayOfMonth),
                (byte) (dayOfWeek - 1),
                (byte) (hour),
                (byte) (minute),
                (byte) (second),
                (byte) (hs),
//                (byte) ((deviation>>8)&0xFF),
//                (byte) (deviation&0xFF),
                (byte) 0x00,
                (byte) 0x00,
                (byte)status
            };
        
    }

    protected int size() {
        return 12;
    }
    
    public void setValue(Calendar dateTime) {
        setValue(dateTime, (byte)0);
    }
    
    public void setValue(Calendar dateTime, byte status) {
        this.dateTime = dateTime;
        this.status = status;
    }
    
    public Calendar getValue( ){
        return dateTime;
    }
    
    public boolean isInvalid() {
        return (status & 0x01) > 0;
    }
    
    public boolean isDoubtful() {
        return (status & 0x02) > 0;
    }
    
    public boolean isDifferentClockBase() {
        return (status & 0x04) > 0;
    }
    
    public boolean isInvalidClockStatus() {
        return (status & 0x08) > 0;
    }
    
    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(getValue().getTime().getTime());
    }
    
    public int intValue() {
        return (int)(getValue().getTime().getTime()/1000);
    }
    
    public long longValue() {
        return getValue().getTime().getTime();
    }
    
    public static void main(String[] args) {
 //        try {
//        	Calendar cal = Calendar.getInstance();
//        	cal.add(Calendar.DATE, -1);
//	        AXDRDateTime dt2 = new AXDRDateTime(cal); //TimeZone.getTimeZone("ECT"));
//	        System.out.println(ProtocolUtils.outputHexString(dt2.getBEREncodedByteArray()));
//	        byte[] data = dt2.getBEREncodedByteArray();
//	        AXDRDateTime dt3 = new AXDRDateTime(data,0,TimeZone.getTimeZone("ECT"));
//	        System.out.println(dt3.getValue().getTime());
//        }
//        catch(IOException e) {
//        	e.printStackTrace();
//        }
        
    	byte[] b = DLMSUtils.hexStringToByteArray("090C07D9041B0108032F00FF8880");
    	try {
			AXDRDateTime dt = new AXDRDateTime(b,0,null);
			
			dt.doGetBEREncodedByteArray();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
}
