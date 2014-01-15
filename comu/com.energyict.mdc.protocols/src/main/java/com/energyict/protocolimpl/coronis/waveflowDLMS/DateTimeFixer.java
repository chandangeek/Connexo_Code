package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocols.util.ProtocolUtils;

import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 7/03/12
 * Time: 14:19
 * <p/>
 * See JIRA ticket COMMUNICATION-738 for more info
 * This class solves a problem with the datetime format of the old AS253/1253 WaveFlow modules.
 */
public class DateTimeFixer {

    /**
     * Fix the timestamp format and return the timestamp that is closest to the system time.
     */
    public static Date getCorrectedDateTimeFromOctetString(OctetString timeStamp, TimeZone timeZone) {
        OctetString timeStamp2 = inverseTimeStampFields(timeStamp);
        Date originalTime = getDateFromOctetString(timeStamp, timeZone);
        Date fixedTime = getDateFromOctetString(timeStamp2, timeZone);
        return getDiff(originalTime) < getDiff(fixedTime) ? originalTime : fixedTime;
    }

    private static Date getDateFromOctetString(OctetString timeStamp, TimeZone timeZone) {
        return new DateTime(timeStamp, timeZone).getValue().getTime();
    }

    private static long getDiff(Date time) {
        return Math.abs(new Date().getTime() - time.getTime());
    }

    /**
     * This method inverts the day field and the (year - 2000) field in the octetstring.
     * It is necessary because modules with old firmware have a bug in the timestamp format.
     *
     * @param timeStamp
     * @return
     */
    private static OctetString inverseTimeStampFields(OctetString timeStamp) {
        byte[] octetStringBytes = timeStamp.toByteArray().clone();
        int year = ProtocolUtils.getShort(octetStringBytes, 0);
        int day = octetStringBytes[3];

        int newYearValue = day + 2000;
        int newDayValue = year - 2000;

        octetStringBytes[0] = (byte) ((newYearValue & 0xFF00) >> 8);
        octetStringBytes[1] = (byte) ((newYearValue & 0x00FF));
        octetStringBytes[3] = (byte) newDayValue;

        return new OctetString(octetStringBytes);
    }
}