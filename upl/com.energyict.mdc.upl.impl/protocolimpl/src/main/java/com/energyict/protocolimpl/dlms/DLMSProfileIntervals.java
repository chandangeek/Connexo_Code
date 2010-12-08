package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 7-dec-2010
 * Time: 11:26:42
 */
public class DLMSProfileIntervals extends Array {

    /**
     * Represents a bitmasked location of the clock object. Default this is on position 1.
     */
    private final int clockMask;
    /**
     * Represents a bitmasked location of the status object(s). Default this is on position 2.
     */
    private final int statusMask;
    /**
     * Represents the bitmasked location of the channels in the structure. Default this is -1 (meaning all channels that are not clock or status).
     * If you for example only want to store channel 1 and channel 3 and the default structure applies(clock = 1 and status = 2), then the
     * channelMask should be 20 (b00010100)
     */
    private final int channelMask;

    /**
     * The used {@link com.energyict.protocolimpl.base.ProfileIntervalStatusBits}
     */
    private final ProfileIntervalStatusBits profileStatusBits;

    /**
     * Constructor with the default masks enabled:
     * <ul>
     * <li> clockMask = 1 (b0001)
     * <li> statusMask = 2 (b0010)
     * <li> channelMask = -1 (b1111) [{@link #isChannelIndex} will rule out the {@link #clockMask} and {@link #statusMask}]
     * </ul>
     *
     * @param encodedData the raw encoded data of the buffer of the {@link com.energyict.dlms.cosem.ProfileGeneric}
     * @param statusBits the statusbits converter to use
     * @throws IOException when encoding types are not as expected
     */
    public DLMSProfileIntervals(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        this(encodedData, 1, 2, -1, statusBits);
    }

    /**
     * Constructor with non-default masks
     *
     * @param encodedData the raw encoded data of the buffer of the {@link com.energyict.dlms.cosem.ProfileGeneric}
     * @param clockMask   the binary represented mask of the clock index
     * @param statusMask  the binary represented mask of all the status indexes
     * @param channelMask the binary represented mask of all the channel indexes
     * @param statusBits the statusbits converter to use
     * @throws IOException when encoding types are not as expected
     */
    public DLMSProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, 0, 0);
        this.clockMask = clockMask;
        this.statusMask = statusMask;
        this.channelMask = channelMask;
        this.profileStatusBits = statusBits;
    }

    /**
     * Parse the content to a list of IntervalData objects
     *
     * @param profileInterval the interval of the profile
     * @return a list of intervalData
     */
    public List<IntervalData> parseIntervals(int profileInterval) throws IOException {
        List<IntervalData> intervalList = new ArrayList<IntervalData>();
        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {

            for (int i = 0; i < nrOfDataTypes(); i++) {
                Structure element = (Structure) getDataType(i);
                List<Integer> values = new ArrayList<Integer>();

                // still implement the case where you have multiple statuses
                if (getNrOfStatusIndexes() == 1) {
                    for (int d = 0; d < element.nrOfDataTypes(); d++) {
                        if (isClockIndex(d)) {
                            if (element.getDataType(d) instanceof OctetString) {
                                OctetString os = (OctetString) element.getDataType(d);
                                // check if the OctetString contains a date, otherwise just add the profileInterval to the current calendar
                                if (os.getOctetStr().length == 12) {
                                    cal = new AXDRDateTime(os).getValue();
                                } else if (cal != null) {
                                    cal.add(Calendar.SECOND, profileInterval);
                                } else {
                                    throw new IOException("Could not create a correct calender for current interval. IntervalStructure: \r\n" + element);
                                }
                            } else if (element.getDataType(d) instanceof NullData && cal != null) {
                                cal.add(Calendar.SECOND, profileInterval);
                            } else {
                                throw new IOException("Unknown calendar type for current interval. IntervalStructure: \r\n" + element);
                            }
                        } else if (isStatusIndex(d)) {
                            profileStatus = profileStatusBits.getEisStatusCode(element.getDataType(d).intValue());
                        } else if (isChannelIndex(d)) {
                            values.add(element.getDataType(d).intValue());
                        }
                    }
                    if (cal != null) {
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        currentInterval.addValues(values);
                    } else {
                        throw new IOException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                }

                intervalList.add(currentInterval);
            }

        } else {
            // no elements in the intervalObject
        }
        return intervalList;
    }

    /**
     * Check whether the given index is the clockIndex
     *
     * @param index the index to check
     * @return true if the given index is the clockIndex, false otherwise
     */
    protected boolean isClockIndex(int index) {
        return ((this.clockMask >> index) & 0x01) == 1;
    }

    /**
     * Check whether the given index is a valid StatusIndex
     *
     * @param index the index to check
     * @return true if the given index is a valid StatusIndex, false otherwise
     */
    protected boolean isStatusIndex(int index) {
        return ((this.statusMask >> index) & 0x01) == 1;
    }

    /**
     * Check whether the given index is a valid ChannelIndex
     *
     * @param index the index to check
     * @return true if the given index is a valid ChannelIndex, false otherwise
     */
    protected boolean isChannelIndex(int index) {
        if (isClockIndex(index) || isStatusIndex(index)) {
            return false;
        }
        return ((this.channelMask >> index) & 0x01) == 1;
    }

    /**
     * Get the number of status indexes
     *
     * @return the number of status indexes
     */
    protected int getNrOfStatusIndexes() {
        return Integer.bitCount(this.statusMask);
    }

    /**
     * Get the number of channel indexes
     *
     * @return the number of channel indexes
     */
    protected int getNrOfChannelIndexes() {
        return Integer.bitCount(this.channelMask);
    }

    /**
     * Get the number of clock indexes (this should be 1)
     *
     * @return the number of clock indexes
     */
    protected int getNrOfClockIndexes() {
        return Integer.bitCount(this.clockMask);
    }
}
