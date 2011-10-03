package com.elster.protocolimpl.dsfg.objects;

import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.fail;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 30-jun-2010
 * Time: 15:56:21
 * </p>
 */
public class ClockObjectTest {

    /**
     * Test using the Original clockParser with a local time in summertime (Thu Jul 01 2010 10:07:34 GMT+0200 (Romance Daylight Time))
     */
    @Test
    public final void parseClockValueTest(){

        // This time represents the time you read the device Thu Jul 01 10:07:34 CEST 2010
        long readTimeInMillis = Long.valueOf("1277971654000");

        String rawDateTime = "4C2C68E6";                            // this is what the device returned
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");  // this is the local timezone

        Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        currentCalendar.setTimeInMillis(readTimeInMillis);  // this sets the time in milliseconds since 1th Jan 1970 according to GMT

        Date calculatedDate = ClockObject.parseClockValue(rawDateTime, timeZone);
        System.out.println("ReadTime : " + currentCalendar.getTime());
        System.out.println("MeterTime : " + calculatedDate);
        
        Long diff = calculatedDate.getTime() - currentCalendar.getTimeInMillis();

        if(Math.abs(diff / 1000) > 300){
            fail("Test 1 FAILED :: TimeDifference is larger then 5 minutes: " + diff/1000 + " seconds.");
        } else {
            System.out.println("Test 1 was SUCCESSFUL TimeDifference is more or less acceptable : " + + diff/1000 + " seconds.");
        }
    }

    /**
     * Test using the Original clockParser with a local time in wintertime (Tue Mar 23 2010 09:07:34 GMT+0100 (Romance Standard Time))
     */
    @Test
    public final void parseClockValue2Test(){

        // This time represents the time you read the device Thu, 23 Mar 2010 09:07:34 GMT
        long readTimeInMillis = Long.valueOf("1269331654000");

        String rawDateTime = "4BA884D6";                            // this is what the device returned
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");  // this is the local timezone

        Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        currentCalendar.setTimeInMillis(readTimeInMillis);  // this sets the time in milliseconds since 1th Jan 1970 according to GMT

        Date calculatedDate = ClockObject.parseClockValue(rawDateTime, timeZone);
        System.out.println("ReadTime : " + currentCalendar.getTime());
        System.out.println("MeterTime : " + calculatedDate);

        Long diff = calculatedDate.getTime() - currentCalendar.getTimeInMillis();

        if(Math.abs(diff / 1000) > 300){
            fail("Test 2 FAILED :: TimeDifference is larger then 5 minutes: " + diff/1000 + " seconds.");
        } else {
            System.out.println("Test 2 was SUCCESSFUL TimeDifference is more or less acceptable : " + diff/1000 + " seconds.");
        }
    }

}
