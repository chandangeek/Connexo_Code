package com.energyict.protocolimplv2.dlms;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * A default DLMS {@link com.energyict.dlms.cosem.ProfileGeneric} buffer parser to a list of {@link com.energyict.protocol.IntervalData}.
 * Depending on the default or given masks, a profile can be build.
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 7-dec-2010<br/>
 * Time: 11:26:42<br/>
 */
public class DLMSProfileIntervals extends Array {

    /**
     * Indicator for the default clock mask of the capturedObjects
     */
    public static final int DefaultClockMask = 1;
    /**
     * Indicator for the default status mask of the capturedObjects
     */
    public static final int DefaultStatusMask = 2;

    /**
     * Represents a bitmasked location of the clock object. Default this is on position 1.
     */
    protected final int clockMask;
    /**
     * Represents a bitmasked location of the status object(s). Default this is on position 2.
     */
    protected final int statusMask;
    /**
     * Represents the bitmasked location of the channels in the structure. Default this is -1 (meaning all channels that are not clock or status).
     * If you for example only want to store channel 1 and channel 3 and the default structure applies(clock = 1 and status = 2), then the
     * channelMask should be 20 (b00010100)
     */
    protected final int channelMask;

    /**
     * The used {@link com.energyict.protocolimpl.base.ProfileIntervalStatusBits}
     */
    protected final ProfileIntervalStatusBits profileStatusBits;

    protected int profileInterval;

    /**
     * Constructor with the default masks enabled:
     * <ul>
     * <li> clockMask = 1 (b0001)
     * <li> statusMask = 2 (b0010)
     * <li> channelMask = -1 (b1111) [{@link #isChannelIndex} will rule out the {@link #clockMask} and {@link #statusMask}]
     * </ul>
     *
     * @param encodedData the raw encoded data of the buffer of the {@link com.energyict.dlms.cosem.ProfileGeneric}
     * @param statusBits  the statusbits converter to use (if set to null, then the {@link DLMSDefaultProfileIntervalStatusBits} will be used)
     * @throws java.io.IOException when encoding types are not as expected
     */
    public DLMSProfileIntervals(byte[] encodedData, ProfileIntervalStatusBits statusBits) throws IOException {
        this(encodedData, DefaultClockMask, DefaultStatusMask, -1, statusBits);
    }

    /**
     * Constructor with non-default masks
     *
     * @param encodedData the raw encoded data of the buffer of the {@link com.energyict.dlms.cosem.ProfileGeneric}
     * @param clockMask   the binary represented mask of the clock index
     * @param statusMask  the binary represented mask of all the status indexes
     * @param channelMask the binary represented mask of all the channel indexes
     * @param statusBits  the statusbits converter to use (if set to null, then the {@link DLMSDefaultProfileIntervalStatusBits} will be used)
     * @throws java.io.IOException when encoding types are not as expected
     */
    public DLMSProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, 0, 0);
        this.clockMask = clockMask;
        this.statusMask = statusMask;
        this.channelMask = channelMask;
        if (statusBits == null) {
            this.profileStatusBits = new DLMSDefaultProfileIntervalStatusBits();
        } else {
            this.profileStatusBits = statusBits;
        }
    }

    /**
     * Parse the content to a list of IntervalData objects
     *
     * @param profileInterval the interval of the profile
     * @return a list of intervalData
     */
    public List<IntervalData> parseIntervals(int profileInterval) throws IOException {
        return this.parseIntervals(profileInterval, null);
    }

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
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        currentInterval.addValues(values);
                    } else {
                        throw new ProtocolException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
                    }
                } else { // the implementation is different if you have multiple status flags
                    Map<Integer, Integer> statuses = new HashMap<Integer, Integer>();
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
                        currentInterval = new IntervalData(cal.getTime(), profileStatus);
                        for (int j = 0; j < values.size(); j++) {
                            currentInterval.addValue(values.get(j), 0, (statuses.containsKey(j) ? statuses.get(j) : 0));
                        }
                    } else {
                        throw new ProtocolException("Calender can not be NULL for building an IntervalData. IntervalStructure: \r\n" + element);
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
    protected Number getValueFromDataType(AbstractDataType dataType, TimeZone tz) {
        if ((dataType instanceof OctetString) && (dataType.getOctetString() != null)) {
            final DateTime dateTime = dataType.getOctetString().getDateTime(tz);
            if (dateTime == null) {
                return dataType.intValue();
            } else {
                return dateTime.getValue().getTimeInMillis();
            }
        }
        return dataType.longValue();     //To avoid negative int values
    }

    /**
     * Construct the calendar depending on the type of the dataType
     *
     * @param cal      the working Calender in the parser
     * @param dataType the dataType from the rawData
     * @return the new Calendar object
     * @throws java.io.IOException when the dataType is not as expected or the calendar could not be constructed
     */
    protected Calendar constructIntervalCalendar(Calendar cal, AbstractDataType dataType) throws IOException {
        return this.constructIntervalCalendar(cal, dataType, null);
    }

    /**
     * Construct the calendar depending on the type of the dataType
     *
     * @param cal      the working Calender in the parser
     * @param dataType the dataType from the rawData
     * @param timeZone the timezone to be used for constructing the AXDRDateTime object - leave null when the deviation information is present in the octetString
     * @return the new Calendar object
     * @throws java.io.IOException when the dataType is not as expected or the calendar could not be constructed
     */
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

    /**
     * Check whether the given index is the clockIndex
     *
     * @param index the index to check
     * @return true if the given index is the clockIndex, false otherwise
     */
    protected boolean isClockIndex(int index) {
        return new BigInteger(String.valueOf(this.clockMask)).shiftRight(index).and(new BigInteger("1")).intValue() == 1;
    }

    /**
     * Check whether the given index is a valid StatusIndex
     *
     * @param index the index to check
     * @return true if the given index is a valid StatusIndex, false otherwise
     */
    protected boolean isStatusIndex(int index) {
        return new BigInteger(String.valueOf(this.statusMask)).shiftRight(index).and(new BigInteger("1")).intValue() == 1;
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
        return new BigInteger(String.valueOf(this.channelMask)).shiftRight(index).and(new BigInteger("1")).intValue() == 1;
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

    protected int getProfileInterval() {
        return profileInterval;
    }
}
