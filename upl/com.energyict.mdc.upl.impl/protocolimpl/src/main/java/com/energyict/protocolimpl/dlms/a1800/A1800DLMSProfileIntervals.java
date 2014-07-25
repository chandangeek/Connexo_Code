package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * DLMSProfileIntervals class respecting the structure of the profiles of the a1800
 * <p/>
 * Created by heuckeg on 09.07.2014.
 */
@SuppressWarnings("unused")
public class A1800DLMSProfileIntervals extends DLMSProfileIntervals {

    protected final int channelStatusMask;
    protected Long multiplier = null;

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
    public List<IntervalData> parseIntervals(int profileInterval) throws IOException {
        this.profileInterval = profileInterval;
        List<IntervalData> intervalList = new ArrayList<IntervalData>();
        Calendar cal = null;
        IntervalData currentInterval;
        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {
            for (int i = 0; i < nrOfDataTypes(); i++) {
                Structure element = (Structure) getDataType(i);
                List<Long> values = new ArrayList<Long>();
                List<Integer> status = new ArrayList<Integer>();

                for (int d = 0; d < element.nrOfDataTypes(); d++) {
                    if (isClockIndex(d)) {
                        try {
                            cal = constructIntervalCalendar(cal, element.getDataType(d));
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
        }
        return intervalList;
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
