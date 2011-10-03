package com.elster.protocolimpl.dsfg.objects;

import com.elster.protocolimpl.dsfg.ProtocolLink;

import java.io.IOException;
import java.util.*;


/**
 * Implementation of the DSfG Clock Object.<br>
 *
 * @author gh
 * @since 5/25/2010
 */
public class ClockObject extends SimpleObject {

    /**
     * Constructor of ClockObject
     *
     * @param link
     * @param address of ClockObject
     */
    public ClockObject(ProtocolLink link, String address) {
        super(link, address);
    }

    /**
     * Return the actual dateTime of the meter
     *
     * @return the dateTime
     * @throws java.io.IOException
     */
    public Date getDateTime() throws IOException {
        return parseClockValue(getValue(), link.getTimeZone());
    }

    /**
     * Write the current time to the meter
     *
     * @throws java.io.IOException when the write failed
     */
    public void writeClock() throws IOException {
        throw new IOException("writeClock not possible on DSfG clock object");
    }


    /**
     * Construct a {@link java.util.Calendar} based on the raw input data and the given {@link java.util.TimeZone}
     *
     * @param rawDateTime - the raw Date and Time
     * @param timeZone    - the timeZone the rawDateTime is written in
     * @return a calendar with the meter time
     */
    public static Date parseClockValue(String rawDateTime, TimeZone timeZone) {

        Calendar meterCal = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));
        //System.out.println("parseClockValue: curr. time millis = " + meterCal.getTimeInMillis());
        //System.out.println("parseClockValue: cur = " + meterCal.getTime());
        //System.out.println("parseClockValue: meterCal . TimeZone = " + meterCal.getTimeZone().getID());
        //System.out.println("parseClockValue: givenTimeZone = " + timeZone.getID());
        
        long dt = Long.parseLong(rawDateTime, 16) * 1000;

        //System.out.println("parseClockValue: dt = " + rawDateTime);

        meterCal.setTimeInMillis(dt);

        Calendar result = Calendar.getInstance(timeZone);
        result.set(meterCal.get(Calendar.YEAR),
                meterCal.get(Calendar.MONTH),
                meterCal.get(Calendar.DAY_OF_MONTH),
                meterCal.get(Calendar.HOUR_OF_DAY),
                meterCal.get(Calendar.MINUTE),
                meterCal.get(Calendar.SECOND));

        //System.out.println("parseClockValue: cal = " + meterCal.getTime());
        //System.out.println("parseClockValue: res = " + result.getTime());
		
		return result.getTime();
	}
    
    public static long calculateClockValue(Date date) {
    	long d = date.getTime() / 1000;
    	return d;
    }
    
    public static long calendarToRaw(Date date, TimeZone timeZone) {
        Calendar result = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));

        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(date);
        result.set(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
        
        return result.getTimeInMillis() / 1000;
    }
}
