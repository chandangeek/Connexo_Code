/*
 * VDEWTimeStamp.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class VDEWTimeStamp {
    
    static public final int MODE_WINTERTIME=0;
    static public final int MODE_SUMMERTIME=1;
    static public final int MODE_UTCTIME=2;
    
    int mode;
    TimeZone timeZone;
    Calendar calendar;
    
    public VDEWTimeStamp(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
    public void parse(String data) throws IOException {
       parse(data.getBytes()); 
    }

    
    public void parse(String datePart, String timePart) throws IOException {
        parse(datePart.getBytes(),timePart.getBytes());
    }
    
    public void parse(byte[] datePart, byte[] timePart) throws IOException {
       int offset=0;
       TimeZone tz=getTimeZone();
       if ((timePart.length == 4) || (timePart.length == 6))
           offset = 0;
       if ((timePart.length == 5) || (timePart.length == 7)) {
           offset = 1;
           setMode((int)ProtocolUtils.bcd2nibble(timePart,0));
           
           if (getMode() == MODE_UTCTIME)
               tz = TimeZone.getTimeZone("GMT");
           // The other two modes, we use the java timezone. We suppose the configurator has correctly
           // set the device timezone.
       }
       
       calendar = ProtocolUtils.getCleanCalendar(tz);
       
       // absorb the season sign for the datepart
       if (datePart.length == 6)
           offset = 0;
       if (datePart.length == 7)
           offset = 1;
       
       calendar.set(Calendar.YEAR, 2000+(int)ProtocolUtils.bcd2byte(datePart,offset));
       calendar.set(Calendar.MONTH, ProtocolUtils.bcd2byte(datePart,2+offset) -1);
       calendar.set(Calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(datePart,4+offset));
       
       
       calendar.set(Calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(timePart,offset));
       calendar.set(Calendar.MINUTE,(int)ProtocolUtils.bcd2byte(timePart,2+offset));
       if ((timePart.length == 6) || (timePart.length == 7))
          calendar.set(Calendar.SECOND,(int)ProtocolUtils.bcd2byte(timePart,4+offset));
    }
    
    
    public void parse(byte[] data) throws IOException {
       int offset=0;
       TimeZone tz=getTimeZone();
       if ((data.length == 10) || (data.length == 12))
           offset = 0;
       if ((data.length == 11) || (data.length == 13)) {
           offset = 1;
           setMode((int)ProtocolUtils.bcd2nibble(data,0));
           
           if (getMode() == MODE_UTCTIME)
               tz = TimeZone.getTimeZone("GMT");
           // The other two modes, we use the java timezone. We suppose the configurator has correctly
           // set the device timezone.
       }
       
       calendar = ProtocolUtils.getCleanCalendar(tz);
       calendar.set(Calendar.YEAR, 2000+(int)ProtocolUtils.bcd2byte(data,offset));
       calendar.set(Calendar.MONTH, ProtocolUtils.bcd2byte(data,2+offset) -1);
       calendar.set(Calendar.DAY_OF_MONTH,(int)ProtocolUtils.bcd2byte(data,4+offset));
       calendar.set(Calendar.HOUR_OF_DAY,(int)ProtocolUtils.bcd2byte(data,6+offset));
       calendar.set(Calendar.MINUTE,(int)ProtocolUtils.bcd2byte(data,8+offset));
       if ((data.length == 12) || (data.length == 13))
          calendar.set(Calendar.SECOND,(int)ProtocolUtils.bcd2byte(data,10+offset));
    }
    
    /**
     * Getter for property mode.
     * @return Value of property mode.
     */
    public int getMode() {
        return mode;
    }
    
    /**
     * Setter for property mode.
     * @param mode New value of property mode.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }
    
    /**
     * Getter for property timeZone.
     * @return Value of property timeZone.
     */
    public java.util.TimeZone getTimeZone() {
        return timeZone;
    }
    
    static public void main(String[] args) {
        try {
            VDEWTimeStamp vts = new VDEWTimeStamp(TimeZone.getTimeZone("ECT"));
            vts.parse("20501101408");
            System.out.println(vts.getCalendar().getTime()+", "+vts.getMode());
            vts.parse("0501101408");
            System.out.println(vts.getCalendar().getTime()+", "+vts.getMode());
            
            vts.parse("2050210","02208");
            System.out.println(vts.getCalendar().getTime()+", "+vts.getMode());
            
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Getter for property calendar.
     * @return Value of property calendar.
     */
    public java.util.Calendar getCalendar() {
        return calendar;
    }
    
 
    
}
