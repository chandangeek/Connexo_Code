package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.common.ObisCode;

import java.util.List;

/**
 * A CollectedLoadProfileConfiguration identifies a load profile configuration.
 * <ul>
 * <li> ObisCode </li>
 * <li> Interval of the Profile</li>
 * <li> NumberOfChannels</li>
 * <li> UnitsOfChannels</li>
 * <li> Indication whether the meter supports this loadProfile</li>
 * </ul>
 */
public interface CollectedLoadProfileConfiguration extends CollectedData{

    public ObisCode getObisCode();

    public String getMeterSerialNumber();

    public int getProfileInterval();

    public int getNumberOfChannels();

    public List<ChannelInfo> getChannelInfos();

    public boolean isSupportedByMeter();

    public void setChannelInfos(List<ChannelInfo> channelInfos);

    public void setSupportedByMeter(boolean b);

}