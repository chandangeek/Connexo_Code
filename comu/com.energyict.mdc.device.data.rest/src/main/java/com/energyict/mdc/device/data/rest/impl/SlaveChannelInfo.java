package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

/**
 * Represents the simple slave channel info.
 */
public class SlaveChannelInfo {
    public String mrid;
    public Long channelId;

    public static SlaveChannelInfo from(Device dataLoggerSlave, Channel channel) {
        SlaveChannelInfo slaveChannelInfo = new SlaveChannelInfo();
        slaveChannelInfo.mrid = dataLoggerSlave.getmRID();
        slaveChannelInfo.channelId = channel.getId();
        return slaveChannelInfo;
    }
}
