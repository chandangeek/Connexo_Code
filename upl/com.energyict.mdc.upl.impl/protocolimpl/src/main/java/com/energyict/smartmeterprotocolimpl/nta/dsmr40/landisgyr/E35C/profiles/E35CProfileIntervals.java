package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.profiles;

import com.energyict.cbo.Utils;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author sva
 * @since 1/07/2015 - 13:32
 */
public class E35CProfileIntervals extends LGDLMSProfileIntervals {

    public E35CProfileIntervals(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, statusBits);
    }

    public E35CProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
    }

    /**
     * Construct the calendar depending on the type of the dataType
     *
     * @param cal      the working Calender in the parser
     * @param dataType the dataType from the rawData
     * @param timeZone the timezone to be used for constructing the AXDRDateTime object - leave null when the deviation information is present in the octetString
     * @return the new Calendar object
     * @throws IOException when the dataType is not as expected or the calendar could not be constructed
     */
    public Calendar constructIntervalCalendar(Calendar cal, AbstractDataType dataType, TimeZone timeZone) throws IOException {
        if (dataType instanceof OctetString) {
            OctetString os = (OctetString) dataType;
            // check if the OctetString contains a date, otherwise just add the profileInterval to the current calendar
            if (os.getOctetStr().length == 12) {
                if (timeZone == null) {
                    cal = new AXDRDateTime(os, AXDRDateTimeDeviationType.Negative).getValue();
                } else {
                    // Profile data is always transmitted in standard timezone (~ winter timezone without DST)!
                    cal = new AXDRDateTime(os.getBEREncodedByteArray(), 0,  Utils.getStandardTimeZone(timeZone)).getValue();
                }
            } else if (cal != null) {
                cal.add(Calendar.SECOND, profileInterval);
            } else {
                throw new IOException("Could not create a correct calender for current interval.");
            }
        } else if (dataType instanceof NullData && cal != null) {
            // Adjust the calendar to start of next interval
            cal.add(Calendar.SECOND, profileInterval);
            if (isRoundDownToNearestInterval()) {
                Date roundedDate = roundDownToNearestInterval(cal.getTime(), profileInterval / 60);
                cal.setTime(roundedDate);
            }
        } else {
            throw new IOException("Unexpected data type '" + dataType.getClass().getName() + "' for current LP interval timestamp. Expected an OctetString or NullData.");
        }
        return cal;
    }
}