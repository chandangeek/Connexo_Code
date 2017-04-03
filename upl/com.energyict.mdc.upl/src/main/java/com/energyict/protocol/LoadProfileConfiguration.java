package com.energyict.protocol;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

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
     * The identifier of the owning Device
     */
    private final DeviceIdentifier deviceIdentifier;

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

    public LoadProfileConfiguration(ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
        this.obisCode = obisCode;
        this.deviceIdentifier = deviceIdentifier;
        this.supportedByMeter = true;
    }

    public LoadProfileConfiguration(ObisCode obisCode, DeviceIdentifier deviceIdentifier, boolean supported) {
        this.obisCode = obisCode;
        this.deviceIdentifier = deviceIdentifier;
        this.supportedByMeter = supported;
    }

    public ObisCode getObisCode() {
        return this.obisCode;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    public int getNumberOfChannels() {
        return (channelInfos != null) ? channelInfos.size() : 0;
    }

    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    public void setChannelInfos(List<ChannelInfo> channelInfos) {
        this.channelInfos = channelInfos;
    }

    public boolean isSupportedByMeter() {
        return supportedByMeter;
    }

    public void setSupportedByMeter(boolean supportedByMeter) {
        this.supportedByMeter = supportedByMeter;
    }

}