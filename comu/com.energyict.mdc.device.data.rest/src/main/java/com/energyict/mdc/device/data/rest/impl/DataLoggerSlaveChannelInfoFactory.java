package com.energyict.mdc.device.data.rest.impl;

import java.util.Optional;

/**
 * Creating DataLoggerSlaveChannelInfo given a slave channel and a data logger channel
 * Copyrights EnergyICT
 * Date: 10/05/2016
 * Time: 11:14
 */
public class DataLoggerSlaveChannelInfoFactory {

    DataLoggerSlaveChannelInfo from(ChannelInfo dataLoggerChannel, Optional<ChannelInfo> slaveChannel){
        DataLoggerSlaveChannelInfo info = new DataLoggerSlaveChannelInfo();
        info.dataLoggerChannel = dataLoggerChannel;
        if (slaveChannel.isPresent()){
            info.slaveChannel = slaveChannel.get();
        }
        return info;
    }


}
