package com.energyict.protocolimplv2.dlms.itron.em620.profiledata;

import com.energyict.mdc.upl.ProtocolException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class EM620ProfileIntervals extends DLMSProfileIntervals {

    private AbstractDlmsProtocol protocol;

    public EM620ProfileIntervals(AbstractDlmsProtocol protocol, byte[] encodedData, int clockMask, int statusMask, int channelMask,
                                 ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
        this.protocol = protocol;
    }

    @Override
    /**
     * Parse the content to a list of IntervalData objects
     *
     * @param profileInterval the interval of the profile
     * @param timeZone        the TimeZone to be used to construct the intervalCalendar
     *                        - use this when the deviation information is not present in the octet string, otherwise leave this parameter null.
     * @return a list of intervalData
     */
    public List<IntervalData> parseIntervals(int profileInterval, TimeZone timeZone) throws IOException {
        this.profileInterval = profileInterval;
        List<IntervalData> intervalList = new ArrayList<>();
        Calendar cal = null;
        boolean outOfInterval = false;
        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {

            for (int i = 0; i < nrOfDataTypes(); i++) {
                Structure element = (Structure) getDataType(i);
                List<Number> values = new ArrayList<>();

                IntervalData currentInterval;
                if (getNrOfStatusIndexes() <= 1) {
                    for (int d = 0; d < element.nrOfDataTypes(); d++) {
                        if (isClockIndex(d)) {
                            try {
                                cal = constructIntervalCalendar(cal, element.getDataType(d), timeZone);
                            } catch (IOException e) {
                                throw new ProtocolException("IntervalStructure: \r\n" + element + "\r\n" + e.getMessage());
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
                        outOfInterval = isOutOfInterval(cal);
                        cal = roundSeconds(cal);
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        currentInterval.addValues(values);
                    } else {
                        throw new ProtocolException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                } else { // the implementation is different if you have multiple status flags
                    Map<Integer, Integer> statuses = new HashMap<>();
                    for (int d = 0; d < element.nrOfDataTypes(); d++) {
                        if (isClockIndex(d)) {
                            try {
                                cal = constructIntervalCalendar(cal, element.getDataType(d), timeZone);
                            } catch (IOException e) {
                                throw new ProtocolException("IntervalStructure: \r\n" + element + "\r\n" + e.getMessage());
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
                        cal = roundSeconds(cal);
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        for (int j = 0; j < values.size(); j++) {
                            currentInterval.addValue(values.get(j), 0, (statuses.getOrDefault(j, 0)));
                        }
                    } else {
                        throw new ProtocolException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                }
                if(!outOfInterval) {
                    intervalList.add(currentInterval);
                }
            }
        }
        return intervalList;
    }

    private boolean isOutOfInterval(Calendar cal) {
        int intervalInMinutes = profileInterval / 60;
        int currentIntervalMinute = cal.get(Calendar.MINUTE);
        int remainder = currentIntervalMinute % intervalInMinutes;
        if(remainder != 0) {
            protocol.journal("Out of interval timestamp detected: " + cal.getTime() + ". Ignoring the collected value.");
            return true;
        } else {
            return false;
        }
    }

    private Calendar roundSeconds(Calendar cal) {
        int currentIntervalSeconds = cal.get(Calendar.SECOND);
        if (currentIntervalSeconds != 0) {
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal;
    }
}
