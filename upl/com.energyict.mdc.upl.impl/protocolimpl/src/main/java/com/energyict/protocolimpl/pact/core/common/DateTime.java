/*
 * DateTime.java
 *
 * Created on 29 maart 2004, 10:31
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class DateTime {
    
    Date date;
    byte[] data;
    
    /** Creates a new instance of DateTime */
    public DateTime(byte[] data, TimeZone timeZone) throws IOException {
    	if(data != null){
    		this.data=data.clone();
    	}
        int timePacs5Sec = ProtocolUtils.getIntLE(data,2,2);
        int datePacs = ProtocolUtils.getIntLE(data,4,2);
        setDate(PactUtils.getCalendar(datePacs,timePacs5Sec,timeZone).getTime());
    }
    
    public DateTime(Calendar calendar) {
        buildFrame(calendar);
    }
    
    private void buildFrame(Calendar calendar) {
        setData(PactUtils.getPacsTimeDataFrame(calendar));  
    }
    
    static public void main(String[] args) {
        try {
            //b7155820
            //$5f$24$59$20
           DateTime dateTime = new DateTime(new byte[]{0x00,0x00,0x60,0x24,0x59,0x20},TimeZone.getTimeZone("ECT")); 
           System.out.println(dateTime.getDate()); 
           dateTime = new DateTime(new byte[]{0x00,0x00,0x5F,0x24,0x59,0x20},TimeZone.getTimeZone("ECT")); 
           System.out.println(dateTime.getDate()); 
           
           dateTime = new DateTime(Calendar.getInstance(TimeZone.getTimeZone("ECT")));
           byte[] data = dateTime.getData();
           byte[] frame = new byte[10];
           ProtocolUtils.arrayCopy(data,frame,2);
           dateTime = new DateTime(frame,TimeZone.getTimeZone("ECT")); 
           System.out.println(dateTime.getDate()); 
           
        }
        catch(IOException e) {
            e.printStackTrace();   
        }
    }
    
    /** Getter for property date.
     * @return Value of property date.
     *
     */
    public java.util.Date getDate() {
        return date;
    }
    
    /** Setter for property date.
     * @param date New value of property date.
     *
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }
    
    /** Getter for property data.
     * @return Value of property data.
     *
     */
    public byte[] getData() {
        return this.data;
    }
    
    /** Setter for property data.
     * @param data New value of property data.
     *
     */
    public void setData(byte[] data) {
    	if(data != null){
    		this.data = data;
    	}
    }
    
}
