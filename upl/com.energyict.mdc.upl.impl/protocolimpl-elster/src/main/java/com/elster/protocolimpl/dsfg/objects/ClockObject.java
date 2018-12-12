package com.elster.protocolimpl.dsfg.objects;

import com.elster.protocolimpl.dsfg.ProtocolLink;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Implementation of the DSfG Clock Object.<br>
 *
 * @author gh
 * @since 5/25/2010
 */
@SuppressWarnings("unused")
public class ClockObject extends SimpleObject {

    /**
     * Constructor of ClockObject
     *
     * @param link - link to protocol
     * @param address of ClockObject
     */
    public ClockObject(ProtocolLink link, String address) {
        super(link, address);
    }

    /**
     * Return the actual dateTime of the meter
     *
     * @return the dateTime
     * @throws IOException
     */
    public Date getDateTime() throws IOException {
        return localDateToUTC(getValue(), link.getTimeZone());
    }

    /**
     * Write the current time to the meter
     *
     * @throws IOException when the write failed
     */
    public void writeClock() throws IOException {
        throw new IOException("writeClock not possible on DSfG clock object");
    }

    /**
     * Construct a {@link Calendar} based on the raw input data and the given {@link TimeZone}
     *
     * @param rawDateTime - the raw Date and Time
     * @param timeZone    - the timeZone the rawDateTime is written in
     * @return a calendar with the meter time
     */
    @SuppressWarnings("MagicConstant")
    public static Date localDateToUTC(String rawDateTime, TimeZone timeZone) {

        Calendar meterCal = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));

        long dt = Long.parseLong(rawDateTime, 16) * 1000;

        meterCal.setTimeInMillis(dt);

        Calendar result = Calendar.getInstance(timeZone);
        result.set(meterCal.get(Calendar.YEAR),
                meterCal.get(Calendar.MONTH),
                meterCal.get(Calendar.DAY_OF_MONTH),
                meterCal.get(Calendar.HOUR_OF_DAY),
                meterCal.get(Calendar.MINUTE),
                meterCal.get(Calendar.SECOND));
        result.set(Calendar.MILLISECOND, 0);

		return result.getTime();
	}

    @SuppressWarnings("MagicConstant")
    public static Date localDateToUTC(long date, TimeZone timeZone) {

        Calendar meterCal = Calendar.getInstance(TimeZone.getTimeZone("GMT0"));

        meterCal.setTimeInMillis(date * 1000);

        Calendar result = Calendar.getInstance(timeZone);
        result.set(meterCal.get(Calendar.YEAR),
                meterCal.get(Calendar.MONTH),
                meterCal.get(Calendar.DAY_OF_MONTH),
                meterCal.get(Calendar.HOUR_OF_DAY),
                meterCal.get(Calendar.MINUTE),
                meterCal.get(Calendar.SECOND));
        result.set(Calendar.MILLISECOND, 0);

        return result.getTime();
    }

    public static long calculateClockValue(Date date)
    {
    	return date.getTime() / 1000;
    }

    @SuppressWarnings("MagicConstant")
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

    public static Date checkDate(Date currDate, Date prevDate, TimeZone timeZone)
    {
        if ((prevDate != null) && (prevDate.getTime() != 0))
        {
            boolean b1 = timeZone.inDaylightTime(currDate);
            boolean b2 = timeZone.inDaylightTime(prevDate);
            if (!b1 && b2)
            {
                if (((currDate.getTime() - prevDate.getTime()) > timeZone.getDSTSavings()) &&
                        ((currDate.getTime() - prevDate.getTime()) <= (timeZone.getDSTSavings() + 3600000)))
                {
                    currDate.setTime(currDate.getTime() - timeZone.getDSTSavings());
                }
            }
        }
        return currDate;
    }
}
