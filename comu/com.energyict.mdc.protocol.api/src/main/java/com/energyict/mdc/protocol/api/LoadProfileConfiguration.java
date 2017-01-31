/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.List;

public class LoadProfileConfiguration {

    /**
     * The <CODE>ObisCode</CODE> of this LoadProfileConfiguration
     */
    private final ObisCode obisCode;

    /**
     * The identifier of the owning Device
     */
    private final DeviceIdentifier<?> deviceIdentifier;

    /**
     * The interval (in seconds) of this LoadProfileConfiguration
     */
    private int profileInterval;

    /**
     * A <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the channels of this LoadProfileConfiguration
     */
    private List<ChannelInfo> channelInfos;

    /**
     * Indication whether the LoadProfileConfiguration is supported by the meter
     */
    private boolean supportedByMeter;

    /**
     * Default constructor. {@link #supportedByMeter} will default be set to true
     *  @param obisCode          the LoadProfileObisCode for this configuration
     * @param deviceIdentifier the serialNumber of the master of this LoadProfileConfiguration
     */
    public LoadProfileConfiguration(ObisCode obisCode, DeviceIdentifier<?> deviceIdentifier) {
        this.obisCode = obisCode;
        this.deviceIdentifier = deviceIdentifier;
        this.supportedByMeter = true;
    }

    /**
     * Default constructor. {@link #supportedByMeter} will default be set to true
     *  @param obisCode          the LoadProfileObisCode for this configuration
     * @param deviceIdentifier the serialNumber of the master of this LoadProfileConfiguration
     * @param supported         indicates whether the LoadProfileConfiguration is supported by the Device
     */
    public LoadProfileConfiguration(ObisCode obisCode, DeviceIdentifier<?> deviceIdentifier, boolean supported) {
        this.obisCode = obisCode;
        this.deviceIdentifier = deviceIdentifier;
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
     * Getter for the {@link #deviceIdentifier}
     *
     * @return {@link #deviceIdentifier}
     */
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return deviceIdentifier;
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
