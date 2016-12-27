package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.io.NestedIOException;

import com.elster.protocolimpl.lis100.ProtocolLink;
import com.elster.protocolimpl.lis100.objects.api.IClockObject;
import com.energyict.dialer.connection.ConnectionException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Interface for clock object
 * <p/>
 * User: heuckeg
 * Date: 24.01.11
 * Time: 15:26
 */
public class ClockObject extends AbstractObject implements IClockObject {

    /**
     * SimpleDateFormat class to convert LIS200 date to normal date
     */
    /* (defined as static to speed up conversion) */
    private static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyMMddHHmmss");

    public ClockObject(ProtocolLink link, byte readOrder, byte writeOrder) {
        super(link, readOrder, writeOrder);
    }

    /**
     * Construct a {@link java.util.Calendar} based on the raw input data and the given {@link TimeZone}
     *
     * @param rawDateTime  - the raw Date and Time
     * @param isSummerTime - flag if given time is in DST
     * @param timeZone     - the timeZone the rawDateTime is written in
     * @return a calendar with the meter time
     */
    public static Calendar parseClockValue(String rawDateTime, boolean isSummerTime, TimeZone timeZone) {

        Calendar meterCal = Calendar.getInstance(timeZone);

        try {
            sdf.setTimeZone(timeZone);
            meterCal.setTime(sdf.parse(rawDateTime));

            /*
             * this piece of code corrects time when switching from summer to
             * winter time, and given time stamp is in first of double hour,
             * or time zone has not DST and device is working with DST
             */
            if ((meterCal.get(Calendar.DST_OFFSET) == 0) && isSummerTime) {
                meterCal.add(Calendar.HOUR_OF_DAY, -1);
            }
        } catch (ParseException e) {
            meterCal = null;
        }

        return meterCal;
    }

    /**
     * Construct a string in the <i>yyMMDDHHmmss</i> format
     *
     * @param date     - date to convert to raw data
     * @param timeZone - device time zone
     * @return the date raw data
     */
    public static String getRawDateTime(Date date, TimeZone timeZone) {

        sdf.setTimeZone(timeZone);
        return sdf.format(date);

    }

    public Date getDate() throws NestedIOException, ConnectionException {
        return parseClockValue(super.getValue(), false, link.getTimeZone()).getTime();
    }

    /**
     * Write the current time to the meter
     *
     * @param date - date to set
     * @throws IOException - when the write failed
     */
    public void setDate(Date date) throws IOException {
        writeValue(getRawDateTime(date, link.getTimeZone()));
    }
}
