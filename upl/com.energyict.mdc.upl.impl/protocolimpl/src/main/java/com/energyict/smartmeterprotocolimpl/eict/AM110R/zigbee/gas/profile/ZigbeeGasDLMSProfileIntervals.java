package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.profile;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.*;

/**
 * @author sva
 * @since 21/12/12 - 11:49
 */

public class ZigbeeGasDLMSProfileIntervals extends DLMSProfileIntervals {

    public ZigbeeGasDLMSProfileIntervals(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, statusBits);
    }

    public ZigbeeGasDLMSProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
        this.setRoundDownToNearestInterval(true);
    }

    /**
     * Parse the content to a list of IntervalData objects
     *
     * @param profileInterval the interval of the profile
     * @param timeZone        the TimeZone to be used to construct the intervalCalendar
     *                        - use this when the deviation information is not present in the octet string, otherwise leave this parameter null.
     * @return a list of intervalData
     */
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
                    Map<Integer, Integer> statuses = new HashMap<Integer, Integer>();
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
                            if (value.intValue() == -1) {
                                statuses.put(values.size(), IntervalStateBits.MISSING);
                                values.add(0);
                            }  else {
                                values.add(value);
                            }
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
                            if (value.intValue() == -1) {
                                statuses.put(values.size(), IntervalStateBits.MISSING);
                                values.add(0);
                            } else {
                                values.add(value);
                            }
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

                intervalList.add(currentInterval);
            }
        }
        return intervalList;
    }

    /**
     * Get the numerical value of a given data type.
     *
     * @param dataType The data type to get the value from
     * @param tz       The timezone to use if there are dates involved
     * @return The numerical value of the data type
     */
    public Number getValueFromDataType(AbstractDataType dataType, TimeZone tz) {
        if ((dataType instanceof OctetString) && (dataType.getOctetString() != null)) {
            final DateTime dateTime = dataType.getOctetString().getDateTime(tz);
            if (dateTime == null) {
                return dataType.intValue();
            } else {
                return dateTime.getValue().getTimeInMillis();
            }
        }
        final int type = dataType.intValue();
        return type;     //To avoid negative int values
    }
}
