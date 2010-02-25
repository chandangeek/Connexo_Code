/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;


/**
 * @author gna
 * @since 23-feb-2010
 *
 */
public class ClockObjectTest {

    @Test
    public final void parseCalendarTest(){
	String actualTime = "1266937680000";
	ClockObject clockObject = new ClockObject(null);
	String rawDateTime = "2010-02-23,15:08:00";
	Calendar meterCalendar = clockObject.parseCalendar(rawDateTime);
	assertEquals(Long.valueOf(actualTime), new Long(meterCalendar.getTimeInMillis()));
    }
    
    @Test
    public final void getRawDataTest(){
	String actualData = "2010-02-23,15:08:00";
	String actualTime = "1266937680000";
	ClockObject clockObject = new ClockObject(null);
	Calendar newCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	newCalendar.setTimeInMillis(Long.valueOf(actualTime));
	assertEquals(actualData, clockObject.getRawData(newCalendar));
    }
    
}
