package com.energyict.mdc.upl.meterdata;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

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
public interface CollectedLoadProfileConfiguration extends CollectedData {

    ObisCode getObisCode();

    String getMeterSerialNumber();

    int getProfileInterval();

    void setProfileInterval(int profileInterval);

    int getNumberOfChannels();

    List<ChannelInfo> getChannelInfos();

    void setChannelInfos(List<ChannelInfo> channelInfos);

    boolean isSupportedByMeter();

    void setSupportedByMeter(boolean supportedByMeter);

}