/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.profiles;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * <p>Custom DLMSProfileIntervals implementation specific for the Xemex ReMI device.</p>
 * <p>The Xemex ReMI device sends its loadProfile without gaps - if no data is available (e.g. no data because of a power down),
 * the device will fill its loadProfile with dummy data. This is done because some head-end systems can not manage loadProfiles with gaps.
 * EIServer however can manage, so during parsing of the intervalData, the dummy data will be filtered out
 * (and thus not saved in EIServer, making it a proper 'missing' interval).
 * </p>
 * <p/>
 * <B>Warning: should not be used for any other devices as this contains custom validation only applicable to Xemex ReMI!</b>
 *
 * @author sva
 * @since 24/05/13 - 13:53
 */
public class XemexDSMRProfileIntervals extends LGDLMSProfileIntervals {

    private static final long EPOCH_SECONDS_01_JAN_2000_GMT = 946684800L;

    public XemexDSMRProfileIntervals(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, statusBits);
    }

    public XemexDSMRProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
    }

    @Override
    public List<IntervalData> parseIntervals(int profileInterval, TimeZone timeZone) throws IOException {
        this.profileInterval = profileInterval;
        List<IntervalData> intervalList = new ArrayList<IntervalData>();
        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {

            for (int i = 0; i < nrOfDataTypes(); i++) {
                Structure element = (Structure) getDataType(i);
                List<Number> values = new ArrayList<Number>();

                if (getNrOfStatusIndexes() <= 1) {
                    for (int d = 0; d < element.nrOfDataTypes(); d++) {
                        if (isClockIndex(d)) {
                            try {
                                cal = constructIntervalCalendar(cal, element.getDataType(d), timeZone);
                            } catch (IOException e) {
                                throw new IOException("IntervalStructure: \r\n" + element + "\r\n" + e.getMessage());
                            }
                        } else if (isStatusIndex(d)) {
                            profileStatus = profileStatusBits.getEisStatusCode(element.getDataType(d).intValue());
                        } else if (isChannelIndex(d)) {
                            final AbstractDataType dataType = element.getDataType(d);
                            final Number value = getValueFromDataType(dataType, timeZone);
                            values.add(value);
                        }
                    }
                    if (cal != null) {
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        currentInterval.addValues(values);
                    } else {
                        throw new IOException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                } else { // the implementation is different if you have multiple status flags
                    Map<Integer, Integer> statuses = new HashMap<Integer, Integer>();
                    for (int d = 0; d < element.nrOfDataTypes(); d++) {
                        if (isClockIndex(d)) {
                            try {
                                cal = constructIntervalCalendar(cal, element.getDataType(d), timeZone);
                            } catch (IOException e) {
                                throw new IOException("IntervalStructure: \r\n" + element + "\r\n" + e.getMessage());
                            }
                        } else if (isStatusIndex(d)) {
                            statuses.put(values.size(), profileStatusBits.getEisStatusCode(element.getDataType(d).intValue()));
                            // we add all the statuses on the 'main' profileStatus
                            profileStatus |= profileStatusBits.getEisStatusCode(element.getDataType(d).intValue());
                        } else if (isChannelIndex(d)) {
                            final AbstractDataType dataType = element.getDataType(d);
                            final Number value = getValueFromDataType(dataType, timeZone);
                            values.add(value);
                        }
                    }

                    if (cal != null) {
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        for (int j = 0; j < values.size(); j++) {
                            currentInterval.addValue(values.get(j), 0, (statuses.containsKey(j) ? statuses.get(j) : 0));
                        }
                    } else {
                        throw new IOException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                }

                if (isValidIntervalData(currentInterval)) {
                    intervalList.add(currentInterval);
                }
            }
        }
        return intervalList;
    }

    /**
     * Check to see if the given IntervalData is valid.
     * Data is not valid under following conditions:
     * <ul>
     *  <li>The profileIntervalStatusBit DATA_NOT_VALID is set</li>
     *  <li>IntervalValue for channel 1 (consumption) is 0</li>
     *  <li>IntervalValue for channel 2 (EPOCH timestamp) contains 946684800000L (01 Jan 2000 00:00:00 GMT)</li>
     * </ul>
     *
     * @param intervalData
     * @return
     */
    private boolean isValidIntervalData(IntervalData intervalData) {
        // Just for safety reasons - If the intervalData has an unexpected number of intervalValues (!= 2), then consider data valid
        if (intervalData.getIntervalValues().size() != 2) {
            return true;
        }

        if ((intervalData.getEiStatus() & IntervalStateBits.CORRUPTED) == IntervalStateBits.CORRUPTED) {
            IntervalValue intervalValue_CHN1 = (IntervalValue) intervalData.getIntervalValues().get(0);
            IntervalValue intervalValue_CHN2 = (IntervalValue) intervalData.getIntervalValues().get(1);
            if ((intervalValue_CHN1.getNumber().longValue() == 0L) &&
                    (intervalValue_CHN2.getNumber().longValue() == EPOCH_SECONDS_01_JAN_2000_GMT)) {
                // IntervalData is not valid
                return false;
            }
        }

        return true;
    }
}
