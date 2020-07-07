package com.energyict.protocolimplv2.dlms;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DLMSProfileIntervalsWithIntermediates extends DLMSProfileIntervals {

    private static final int MILLIS = 1000;
    private static final int SECONDS = 60;

    public DLMSProfileIntervalsWithIntermediates(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, statusBits);
    }

    public DLMSProfileIntervalsWithIntermediates(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
    }

    /**
     * Construct the calendar depending on the type of the dataType
     *
     * @param cal      the working Calender in the parser
     * @param dataType the dataType from the rawData
     * @param timeZone the timezone to be used for constructing the AXDRDateTime object - leave null when the deviation information is present in the octetString
     * @return the new Calendar object
     * @throws java.io.IOException when the dataType is not as expected or the calendar could not be constructed
     */
    protected Calendar constructIntervalCalendar(Calendar cal, AbstractDataType dataType, TimeZone timeZone) throws IOException {
        if (dataType instanceof OctetString) {
            OctetString os = (OctetString) dataType;
            // check if the OctetString contains a date, otherwise just add the profileInterval to the current calendar
            if (os.getOctetStr().length == 12) {
                if (timeZone == null) {
                    cal = new AXDRDateTime(os, AXDRDateTimeDeviationType.Negative).getValue();
                } else {
                    cal = new AXDRDateTime(os.getBEREncodedByteArray(), 0, timeZone).getValue();
                }

                adjustIntervalCalendar(cal);

            } else if (cal != null) {
                cal.add(Calendar.SECOND, profileInterval);
            } else {
                throw new ProtocolException("Could not create a correct calender for current interval.");
            }
        } else if (dataType instanceof NullData && cal != null) {
            cal.add(Calendar.SECOND, profileInterval);
        } else {
            throw new ProtocolException("Unknown calendar type for current interval.");
        }
        return cal;
    }

    private void adjustIntervalCalendar(Calendar cal) {
        int intervalInMinutes = profileInterval / 60;
        int currentIntervalMinute = cal.get(Calendar.MINUTE);
        int remainder = currentIntervalMinute % intervalInMinutes;
        if (remainder != 0) {
            // Adjust the calendar to start of next interval
            cal.add(Calendar.SECOND, profileInterval);
            Date roundedDate = roundUpToNearestInterval(cal.getTime(), intervalInMinutes);
            cal.setTime(roundedDate);
        }
    }

    /**
     * @param timeStamp
     * @param intervalInMinutes
     * @return
     */
    protected Date roundUpToNearestInterval(Date timeStamp, int intervalInMinutes) {
        long intervalMillis = (long) intervalInMinutes * MILLIS * SECONDS;

        Calendar cal = Calendar.getInstance();
        cal.setTime(timeStamp);
        if (Math.abs(intervalInMinutes) >= (31 * 24 * 60)) { // In case of monthly intervals
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        if (Math.abs(intervalInMinutes) >= (24 * 60)) {    // In case of daily/monthly intervals
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }

        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long diff = timeStamp.getTime() - cal.getTimeInMillis();
        long overTime = diff % intervalMillis;
        long beforeTime = intervalMillis - overTime;

        Calendar returnDate = Calendar.getInstance();
        returnDate.setTime(timeStamp);
        if (intervalInMinutes > 0) {
            returnDate.add(Calendar.SECOND, overTime != 0 ? (int) (beforeTime / 1000) : 0);         // The seconds      - split up to avoid int limit
            returnDate.add(Calendar.MILLISECOND, overTime != 0 ? (int) (beforeTime % 1000) : 0);    // The milliseconds
        } else {
            returnDate.add(Calendar.SECOND, (overTime != 0 ? (int) (overTime / 1000) : 0) * (-1));          // The seconds      - split up to avoid int limit
            returnDate.add(Calendar.MILLISECOND, (overTime != 0 ? (int) (overTime % 1000) : 0) * (-1));     // The milliseconds
        }
        return returnDate.getTime();
    }
}
