package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.comserver.commands.NoopDeviceCommand;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;

import java.util.List;

/**
 * Contains the configuration of a meter his <CODE>LoadProfile</CODE>.
 * Examples include:
 * <ul></ul>
 * <li> ObisCode
 * <li> Interval of the Profile
 * <li> NumberOfChannels
 * <li> UnitsOfChannels
 * <li> Indication whether the meter supports this loadProfile
 * </ul
 */
public class DeviceLoadProfileConfiguration extends CollectedDeviceData implements CollectedLoadProfileConfiguration {

    /**
     * The <CODE>ObisCode</CODE> of this {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     */
    private final ObisCode obisCode;

    /**
     * The serialNumber of the <CODE>Device</CODE>
     */
    private final String meterSerialNumber;

    /**
     * The interval (in seconds) of this {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
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
     * @param obisCode the LoadProfileObisCode for this configuration
     * @param meterSerialNumber the serialNumber of the master of this <code>LoadProfile</code>
     */
    public DeviceLoadProfileConfiguration (ObisCode obisCode, String meterSerialNumber) {
        this.obisCode = obisCode;
        this.meterSerialNumber = meterSerialNumber;
        this.supportedByMeter = true;
    }

    /**
     * Default constructor. {@link #supportedByMeter} will default be set to true
     *
     * @param obisCode the LoadProfileObisCode for this configuration
     * @param meterSerialNumber the serialNumber of the master of this <code>LoadProfile</code>
     * @param supported indicates whether the <code>LoadProfile</code> is supported by the Device
     */
    public DeviceLoadProfileConfiguration (ObisCode obisCode, String meterSerialNumber, boolean supported) {
        this.obisCode = obisCode;
        this.meterSerialNumber = meterSerialNumber;
        this.supportedByMeter = supported;
    }

    /**
     * Getter for the {@link #obisCode}
     *
     * @return {@link #obisCode}
     */
    @Override
    public ObisCode getObisCode () {
        return this.obisCode;
    }

    /**
     * Getter for the {@link #meterSerialNumber}
     *
     * @return {@link #meterSerialNumber}
     */
    @Override
    public String getMeterSerialNumber () {
        return meterSerialNumber;
    }

    /**
     * Getter for the {@link #profileInterval}
     *
     * @return {@link #profileInterval}
     */
    @Override
    public int getProfileInterval () {
        return profileInterval;
    }

    /**
     * Setter for the {@link #profileInterval}
     *
     * @param profileInterval the new {@link #profileInterval} to set
     */
    @Override
    public void setProfileInterval (int profileInterval) {
        this.profileInterval = profileInterval;
    }

    /**
     * Getter for the number of channels
     *
     * @return number of channels
     */
    @Override
    public int getNumberOfChannels () {
        return (channelInfos != null) ? channelInfos.size() : 0;
    }

    /**
     * Getter for the {@link #channelInfos}
     *
     * @return {@link #channelInfos}
     */
    @Override
    public List<ChannelInfo> getChannelInfos () {
        return channelInfos;
    }

    /**
     * Setter for the {@link #channelInfos}
     *
     * @param channelInfos the new {@link #channelInfos} to set
     */
    public void setChannelInfos (List<ChannelInfo> channelInfos) {
        this.channelInfos = channelInfos;
    }

    /**
     * Getter for the {@link #supportedByMeter}
     *
     * @return {@link #supportedByMeter}
     */
    @Override
    public boolean isSupportedByMeter () {
        return supportedByMeter;
    }

    /**
     * Setter for the {@link #supportedByMeter}
     *
     * @param supportedByMeter the new {@link #supportedByMeter} to set
     */
    public void setSupportedByMeter (boolean supportedByMeter) {
        this.supportedByMeter = supportedByMeter;
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new NoopDeviceCommand();
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectLoadProfileData();
    }

}