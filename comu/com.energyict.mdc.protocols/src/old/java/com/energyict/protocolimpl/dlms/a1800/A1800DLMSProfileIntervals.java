package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * DLMSProfileIntervals class respecting the structure of the profiles of the a1800
 * <p/>
 * Created by heuckeg on 09.07.2014.
 */
@SuppressWarnings("unused")
public class A1800DLMSProfileIntervals extends DLMSProfileIntervals {

    protected final int channelStatusMask;
    protected Long multiplier = null;
    private static final byte[] INVALID_DATETIME_OCTET_STRING_BYTES = new byte[12];

    public A1800DLMSProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        this(encodedData, clockMask, statusMask, channelMask, 0, statusBits);
    }

    public A1800DLMSProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, int channelStatusMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
        this.channelStatusMask = channelStatusMask;
    }

    public void setMultiplier(long multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Parse the content to a list of IntervalData objects
     *
     * @param profileInterval the interval of the profile
     * @return a list of intervalData
     */
    public List<IntervalData> parseIntervals(int profileInterval, TimeZone timeZone) throws IOException {
        this.profileInterval = profileInterval;
        List<IntervalData> intervalList = new ArrayList<IntervalData>();
        Calendar cal = null;
        IntervalData currentInterval;
        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {
            removeCurrentIntervalIndexes();
            boolean jumpToNext = false;
            for (int i = 0; i < nrOfDataTypes(); i++) {
                Structure element = (Structure) getDataType(i);
                List<Long> values = new ArrayList<Long>();
                List<Integer> status = new ArrayList<Integer>();

                for (int d = 0; d < element.nrOfDataTypes(); d++) {
                    if (isClockIndex(d)) {
                        try {
                            cal = constructIntervalCalendar(cal, element.getDataType(d), timeZone);
                            if (cal == null) {
                                jumpToNext = true; // The current element is invalid, so no need to parse it, but instead jump to the next element
                                break;
                            }
                        } catch (IOException e) {
                            throw new IOException("IntervalStructure: \r\n" + element + "\r\n" + e.getMessage());
                        }
                    } else if (isStatusIndex(d)) {
                        profileStatus = getEisStatusCode(element.getDataType(d).intValue());
                    } else if (isChannelStatusIndex(d)) {
                        AbstractDataType adt = element.getDataType(d);
                        long state = adt.longValue();
                        fillChannelStatusArray(state, status);
                    } else if (isChannelIndex(d)) {
                        long vr = element.getDataType(d).longValue();
                        long vc = multiplier == null ? vr : vr * multiplier;
                        values.add(vc);
                    }
                }
                if (!jumpToNext) {
                    if (cal != null) {
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        for (int j = 0; j < values.size(); j++) {
                            int channelStatus = (j < status.size()) ? status.get(j) : 0;
                            currentInterval.addValue(values.get(j), 0, channelStatus);
                        }
                    } else {
                        throw new IOException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                    intervalList.add(currentInterval);
                }
                jumpToNext = false;
            }
        }
        return intervalList;
    }

    @Override
    public Calendar constructIntervalCalendar(Calendar cal, AbstractDataType dataType, TimeZone timeZone) throws IOException {
        if (dataType instanceof OctetString) {
            OctetString os = (OctetString) dataType;
            // check if the OctetString contains a date, otherwise just add the profileInterval to the current calendar
            if (os.getOctetStr().length == 12) {
                if (Arrays.equals(INVALID_DATETIME_OCTET_STRING_BYTES, os.getOctetStr())) {
                    return null;
                }

                if (timeZone == null) {
                    cal = new AXDRDateTime(os, AXDRDateTimeDeviationType.Negative).getValue();
                } else {
                    cal = new AXDRDateTime(os.getBEREncodedByteArray(), 0, timeZone).getValue();
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

    /**
     * <p>When requesting profile data, the device sends all data in buffer,
     * but it also sends a snapshot of current index alongside.</p>
     * This data should be filtered out, as it should not be stored in EIMaster.
     * <BR>
     * E.g.: readout at 13h:41m of 15min profile
     * <ul>
     *  <li>13:00</li>
     *  <li>13:15</li>
     *  <li>13:30</li>
     *  <li>13:30</li>
     *  <li><b>13:41</b></li>
     *  </ul>
     *
     */
    private void removeCurrentIntervalIndexes() {
        getAllDataTypes().remove(0);    // The current index is always the last one
    }

    private int getEisStatusCode(int lineStatus) {
        int eiStatus = IntervalStateBits.OK;
        if ((lineStatus & 0x0001) == 0) {
            eiStatus = IntervalStateBits.CORRUPTED;
        }
        if ((lineStatus & 0x000C) != 0) {
            if ((lineStatus & 0x000C) == 0xC) {
                eiStatus |= IntervalStateBits.MISSING;
            } else {
                eiStatus |= IntervalStateBits.SHORTLONG;
            }
        }
        if ((lineStatus & 0x0020) != 0) {
            eiStatus = IntervalStateBits.POWERDOWN;
        }
        if ((lineStatus & 0x00C0) != 0) {
            eiStatus = IntervalStateBits.SHORTLONG;
        }
        return eiStatus;
    }

    private boolean isChannelStatusIndex(int index) {
        return ((1 << index) & channelStatusMask) != 0;
    }

    private int fillChannelStatusArray(long allChannelStatus, List<Integer> status) {
        int sumStatus = 0;
        for (int i = 0; i < 8; i++) {
            int eiStatus = 0;
            int channelStatus = (int) ((allChannelStatus >> ((long) (4 * i))) & 0xF);
            if ((channelStatus & 0x1) != 0) {
                eiStatus |= IntervalStateBits.TEST;
            }
            if ((channelStatus & 0x2) != 0) {
                eiStatus |= IntervalStateBits.OVERFLOW;
            }
            sumStatus |= eiStatus;
            status.add(i, eiStatus);
        }
        return sumStatus;
    }
}
