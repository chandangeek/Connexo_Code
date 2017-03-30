package com.energyict.protocol;

import com.energyict.obis.ObisCode;

import java.util.List;

/**
 *
 * Date: 9/23/13
 * Time: 11:05 AM
 */
public class LoadProfileConfiguration {

    /**
     * The <CODE>ObisCode</CODE> of this LoadProfile
     */
    private final ObisCode obisCode;

    /**
     * The serialNumber of the <CODE>Device</CODE>
     */
    private final String meterSerialNumber;

    /**
     * The interval (in seconds) of this LoadProfile
     */
    private int profileInterval;

    /**
     * A <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the channels of the <CODE>LoadProfile</CODE
     */
    private List<ChannelInfo> channelInfos;

    /**
     * Indication whether the <CODE>LoadProfile</CODE> is supported by the meter
     */
    private boolean supportedByMeter;

    /**
     * Default constructor. {@link #supportedByMeter} will default be set to true
     *
     * @param obisCode          the LoadProfileObisCode for this configuration
     * @param meterSerialNumber the serialNumber of the master of this <code>LoadProfile</code>
     */
    public LoadProfileConfiguration(ObisCode obisCode, String meterSerialNumber) {
        this.obisCode = obisCode;
        this.meterSerialNumber = meterSerialNumber;
        this.supportedByMeter = true;
    }

    /**
     * Default constructor. {@link #supportedByMeter} will default be set to true
     *
     * @param obisCode          the LoadProfileObisCode for this configuration
     * @param meterSerialNumber the serialNumber of the master of this <code>LoadProfile</code>
     * @param supported         indicates whether the <code>LoadProfile</code> is supported by the Device
     */
    public LoadProfileConfiguration(ObisCode obisCode, String meterSerialNumber, boolean supported) {
        this.obisCode = obisCode;
        this.meterSerialNumber = meterSerialNumber;
        this.supportedByMeter = supported;
    }

    /**
     * Getter for the {@link #obisCode}
     *
     * @return {@link #obisCode}
     */
    public ObisCode getObisCode() {
        return this.obisCode;
    }

    /**
     * Getter for the {@link #meterSerialNumber}
     *
     * @return {@link #meterSerialNumber}
     */
    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    /**
     * Getter for the {@link #profileInterval}
     *
     * @return {@link #profileInterval}
     */
    public int getProfileInterval() {
        return profileInterval;
    }

    /**
     * Setter for the {@link #profileInterval}
     *
     * @param profileInterval the new {@link #profileInterval} to set
     */
    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    /**
     * Getter for the number of channels
     *
     * @return number of channels
     */
    public int getNumberOfChannels() {
        return (channelInfos != null) ? channelInfos.size() : 0;
    }

    /**
     * Getter for the {@link #channelInfos}
     *
     * @return {@link #channelInfos}
     */
    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    /**
     * Setter for the {@link #channelInfos}
     *
     * @param channelInfos the new {@link #channelInfos} to set
     */
    public void setChannelInfos(List<ChannelInfo> channelInfos) {
        this.channelInfos = channelInfos;
    }

    /**
     * Getter for the {@link #supportedByMeter}
     *
     * @return {@link #supportedByMeter}
     */
    public boolean isSupportedByMeter() {
        return supportedByMeter;
    }

    /**
     * Setter for the {@link #supportedByMeter}
     *
     * @param supportedByMeter the new {@link #supportedByMeter} to set
     */
    public void setSupportedByMeter(boolean supportedByMeter) {
        this.supportedByMeter = supportedByMeter;
    }


}
