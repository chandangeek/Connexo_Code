package com.energyict.protocolimplv2.dlms.acud.profiledata;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class AcudProfileIntervals extends DLMSProfileIntervals {

    private final ObisCode loadProfileObisCode;

    public AcudProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask,
                                ProfileIntervalStatusBits statusBits, ObisCode loadProfileObisCode) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
        this.loadProfileObisCode = loadProfileObisCode;
    }

    @Override
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

                // instantaneous LP has interval timestamp equal to the current time on the meter
                // in Connexo we set the interval of the LP to 1 minute, we need to round down the second and millis
                if (this.loadProfileObisCode.equals(AcudLoadProfileDataReader.ELECTRICITY_INSTANTANEOUS_LP)) {
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                }

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

}
