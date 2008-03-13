package com.energyict.dlms.axrdencoding.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocol.ProtocolUtils;

/**
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
 * @author fbo
 *
 */

public class DateTime extends AbstractDataType {

    private Calendar dateTime;
    private int status;
    
    public DateTime( ) { }
    
    /* */
    public DateTime(byte[] berEncodedData, int offset, TimeZone zone) {
        
        
        // !! add type check here !!
        offset = offset + 2;
        
        dateTime = ProtocolUtils.getCleanCalendar(zone);
        
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
        
        return 
            new byte [] {   
                (byte) 0x09,
                (byte) 0x0c,
                (byte) ((year & 0Xff00 ) >> 8),
                (byte) (year & 0X00ff),
                (byte) (month + 1),
                (byte) (dayOfMonth),
                (byte) (dayOfWeek - 1),
                (byte) (hour),
                (byte) (minute),
                (byte) (second),
                (byte) (hs),
                (byte) 0x80,
                0,
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
        
        byte [] ba = new byte [] {
        (byte)0x09, (byte)0x0c, (byte)0x07, (byte)0xD7, (byte)0x0A, 
        (byte)0x16, (byte)0x01, (byte)0x0A, (byte)0x35, (byte)0x0F, 
        (byte)0xFF, (byte)0x08, (byte)0x00, (byte)0x00
        };
        
        
        DateTime dt = new DateTime( ba, 0, TimeZone.getDefault() );
        System.out.println( "" + dt.getValue().getTime() );
        
        Calendar v = dt.getValue();

        int year        = v.get(Calendar.YEAR);
        int month       = v.get(Calendar.MONTH);
        int dayOfMonth  = v.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek   = v.get(Calendar.DAY_OF_WEEK);
        int hour        = v.get(Calendar.HOUR);
        int minute      = v.get(Calendar.MINUTE);
        int second      = v.get(Calendar.SECOND);
        int hms         = v.get(Calendar.MILLISECOND) / 10;
        
        byte [] bin =
            
        {   
            (byte) 0x09,
            (byte) 0x0c,
            (byte) ((year & 0Xff00 ) >> 8),
            (byte) (year & 0X00ff),
            (byte) (month + 1),
            (byte) (dayOfMonth),
            (byte) (dayOfWeek - 1),
            (byte) (hour),
            (byte) (minute),
            (byte) (second),
            (byte) (hms),
            0,
            0,
            0
        };
        
        for (int i = 0; i < bin.length; i++) {
            System.out.print( Integer.toHexString( bin[i] ) + " " );
        }
        System.out.println( bin );
        System.out.println( new DateTime( bin, 0, TimeZone.getDefault() ).getValue().getTime() );
        
        
    }
    
}
