/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.Calendar;

public class LGDLMSProfileIntervals extends DLMSProfileIntervals {

    /**
     * Constructor with the default masks enabled:
     * <ul>
     * <li> clockMask = 1 (b0001)
     * <li> statusMask = 2 (b0010)
     * <li> channelMask = -1 (b1111) [{@link #isChannelIndex} will rule out the {@link #clockMask} and {@link #statusMask}]
     * </ul>
     *
     * @param encodedData the raw encoded data of the buffer of the {@link com.energyict.dlms.cosem.ProfileGeneric}
     * @param statusBits  the statusbits converter to use (if set to null, then the {@link com.energyict.protocolimpl.dlms.DLMSDefaultProfileIntervalStatusBits} will be used)
     * @throws java.io.IOException when encoding types are not as expected
     */
    public LGDLMSProfileIntervals(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, statusBits);
    }

    /**
     * Constructor with non-default masks
     *
     * @param encodedData the raw encoded data of the buffer of the {@link com.energyict.dlms.cosem.ProfileGeneric}
     * @param clockMask   the binary represented mask of the clock index
     * @param statusMask  the binary represented mask of all the status indexes
     * @param channelMask the binary represented mask of all the channel indexes
     * @param statusBits  the statusbits converter to use (if set to null, then the {@link com.energyict.protocolimpl.dlms.DLMSDefaultProfileIntervalStatusBits} will be used)
     * @throws java.io.IOException when encoding types are not as expected
     */
    public LGDLMSProfileIntervals(final byte[] encodedData, final int clockMask, final int statusMask, final int channelMask, final ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Construct the calendar depending on the type of the dataType
     *
     * @param cal      the working Calender in the parser
     * @param dataType the dataType from the rawData
     * @return the new Calendar object
     * @throws java.io.IOException when the dataType is not as expected or the calendar could not be constructed
     */
    @Override
    public Calendar constructIntervalCalendar(Calendar cal, final AbstractDataType dataType) throws IOException {
        if (dataType instanceof OctetString) {
            OctetString os = (OctetString) dataType;
            // check if the OctetString contains a date, otherwise just add the profileInterval to the current calendar
            if (os.getOctetStr().length == 12) {
                cal = new AXDRDateTime(os, AXDRDateTimeDeviationType.Positive).getValue();
            } else if (cal != null) {
                cal.add(Calendar.SECOND, getProfileInterval());
            } else {
                throw new IOException("Could not create a correct calender for current interval.");
            }
        } else if (dataType instanceof NullData && cal != null) {
            cal.add(Calendar.SECOND, getProfileInterval());
        } else {
            throw new IOException("Unknown calendar type for current interval.");
        }
        return cal;
    }
}
