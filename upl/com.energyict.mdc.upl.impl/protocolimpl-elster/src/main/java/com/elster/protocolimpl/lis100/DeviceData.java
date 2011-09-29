package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.connection.Lis100Connection;
import com.elster.protocolimpl.lis100.profile.IIntervalDataStreamReader;
import com.energyict.dialer.connection.ConnectionException;

import java.io.IOException;
import java.util.*;

/**
 * Class containing all readable data of a lis100 device
 * <p/>
 * User: heuckeg
 * Date: 05.08.11
 * Time: 08:47
 */
@SuppressWarnings({"unused"})
public class DeviceData {

    /* link to object factory to retrieve data */
    Lis100ObjectFactory objectFactory;
    /* timezone off device (from EIS) */
    private TimeZone timeZone;

    /* flag if class initialized by calling prepareDeviceData() */
    private boolean initialized = false;

    /* version of device -> also determines device type */
    private int softwareVersion;
    /* String for type of device */
    private String meterType;
    /* amount of possible channelss to readout */
    private int noOfChannels;
    /* */
    private boolean havingCalcType = true;

    private HashMap<Integer, ChannelData> channelData = new HashMap<Integer, ChannelData>();

    /**
     * Constructor for class
     *
     * @param objectFactory - reference to objectFactory to retrieve data
     * @param timeZone      - time zone of device
     */
    public DeviceData(Lis100ObjectFactory objectFactory, TimeZone timeZone) {
        this.objectFactory = objectFactory;
        this.timeZone = timeZone;
    }

    /**
     * prepare class by reading first command data and evaluate them
     *
     * @throws java.io.IOException - in case of an error
     */
    public void prepareDeviceData() throws IOException {
        if (!initialized) {
            softwareVersion = objectFactory.getSoftwareVersionObject().getIntValue();
            computeVersionNumber();
            initialized = true;
        }
    }

    /**
     * gets link to object factory (used by the channels class)
     *
     * @return object factory
     */
    public Lis100ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    /**
     * get time zone of device (used by channels class)
     *
     * @return time zone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * get version of software
     *
     * @return 4 digit version what identifies device & firmware
     */
    public int getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * gets number of channels in device
     *
     * @return number of channels (0..4)
     */
    public int getNumberOfChannels() {
        return noOfChannels;
    }

    /**
     * gets flag if the decive is able to give a calc type value
     *
     * @return true if calc type can be read from device, otherwise false
     */
    public boolean isHavingCalcType() {
        return havingCalcType;
    }

    /**
     * gets type of device as string
     *
     * @return type of device
     * @throws java.io.IOException - in case of an error
     */
    public String getMeterType() throws IOException {
        prepareDeviceData();
        return meterType;
    }

    public int getCurrentChannel() throws IOException {
        if (getNumberOfChannels() > 1) {
            return objectFactory.getCurrentChannelObject().getIntValue();
        } else {
            return 0;
        }
    }

    /**
     * gets channel data for a specific channel.
     * If channel for wanted channel not read, read until channel found and read
     *
     * @param channelNo - number of channel (0..(number of channel - 1))
     * @param until - read interval data until...
     * @param reader - with reader object...
     *
     * @return channel data
     * @throws java.io.IOException - in case of an error
     */
    public ChannelData getChannelData(int channelNo, Date until, IIntervalDataStreamReader reader) throws IOException {

        if ((channelNo < 0) && (channelNo >= noOfChannels)) {
            //TODO: thow exception?
            return null;
        }

        ChannelData result = channelData.get(channelNo);
        // if no data for this channel read OR needed interval data, but not yet read...
        if ((result == null) || ((until != null) && (result.getRawData() == null))) {
            result = readChannelData(channelNo, until, reader);
        }
        return result;
    }

    private ChannelData readChannelData(int channelNo, Date until, IIntervalDataStreamReader reader) throws IOException {

        /* loop through channels until found... */
        int currChannel;
        for (; ; switchToNextChannel(currChannel)) {
            /* channel number of current channel */
            currChannel =  getCurrentChannel();
            /* check if already exists */
            ChannelData result = channelData.get(currChannel);
            if (result == null) {
                /* no, so create a new one */
                result = new ChannelData(this);
                result.readChannelData();
                channelData.put(currChannel, result);
            }
            /* need interval data, but not yet read... */
            if ((until != null) && (result.getRawData() == null)) {
                result.readChannelProfile(until, reader);
            }
            if (currChannel == channelNo) {
                return result;
            }
        }
    }

    public void switchToNextChannel(int currChannel) throws IOException {

        if (getNumberOfChannels() == 1) {
            return;
        }

        Lis100Connection conn = objectFactory.getLink().getLis100Connection();
        for (int i = 0; i < conn.getProtocolRetry(); i++) {
            conn.disconnect();
            conn.connect();
            conn.signon(objectFactory.getLink().getPassword());
            if (objectFactory.currentChannelObject.getIntValue() != currChannel) {
                return;
            }
        }
        throw new ConnectionException("switchToNextChannel(): didn't work", (short) 5);

    }

    private void computeVersionNumber() {
        switch (softwareVersion / 100) {
            case 1:
            case 2:
            case 3:
            case 4:
                meterType = "DS-100/N";
                noOfChannels = 1;
                havingCalcType = false;
                break;
            case 11:
            case 12:
                meterType = "DS-100/B";
                noOfChannels = 1;
                havingCalcType = softwareVersion >= 1223;
                break;
            case 13:
                meterType = "DS-100/B2";
                noOfChannels = 1;
                break;
            case 21:
            case 22:
            case 23:
                meterType = "DS-100/V";
                noOfChannels = 4;
                havingCalcType = softwareVersion >= 2332;
                break;
            case 24:
                meterType = "DS-100/V2";
                noOfChannels = 4;
                break;
            case 25:
                meterType = "DS-100/V3";
                noOfChannels = 4;
                break;
            case 31:
                meterType = "DS-100/E";
                noOfChannels = 4;
                havingCalcType = softwareVersion >= 3111;
                break;
            case 41:
                meterType = "DS-100/A";
                noOfChannels = 4;
                havingCalcType = softwareVersion >= 4111;
                break;
            case 42:
                meterType = "DS-100/M";
                noOfChannels = 8;
                break;
            case 51:
                meterType = "DS-100/C";
                noOfChannels = 4;
                break;
            case 84:
                meterType = "TC-90/T";
                noOfChannels = 2;
                break;
            case 89:
                meterType = "EK-88SNAM";
                noOfChannels = 4;
                break;
            case 90:
                meterType = "EK-86";
                noOfChannels = 4;
                break;
            case 92:
                meterType = "EK-88";
                noOfChannels = 4;
                break;
            case 93:
                meterType = "EK-88N";
                noOfChannels = 4;
                break;
            case 94:
                meterType = "EK-90";
                noOfChannels = 2;
                break;
            case 95:
                meterType = "EK-87N";
                noOfChannels = 4;
                break;
            default:
                meterType = "unknown";
                noOfChannels = 0;
        }
    }


    public Collection<ChannelData> getChannels() {
        return channelData.values();
    }
}
