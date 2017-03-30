/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.Optional;

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
