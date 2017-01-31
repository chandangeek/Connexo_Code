/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

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

    public DeviceIdentifier<?> getDeviceIdentifier();

    public int getProfileInterval();

    public int getNumberOfChannels();

    public List<ChannelInfo> getChannelInfos();

    public boolean isSupportedByMeter();

    public void setChannelInfos(List<ChannelInfo> channelInfos);

    public void setSupportedByMeter(boolean b);

    public void setProfileInterval (int intervalInSeconds);

}