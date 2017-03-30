/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DataLoggerSlaveChannelInfoFactoryTest {

    @Test
    public void fromTestNoSlave(){
        ChannelInfo dataLoggerChannel = new ChannelInfo();

        DataLoggerSlaveChannelInfo info = new DataLoggerSlaveChannelInfoFactory().from(dataLoggerChannel, Optional.empty());

        assertThat(info.dataLoggerChannel).isEqualTo(dataLoggerChannel);
        assertThat(info.slaveChannel).isNull();
        assertThat(info.availabilityDate).isNull();
    }

    @Test
    public void fromTestWithSlave(){
        ChannelInfo dataLoggerChannel = new ChannelInfo();
        ChannelInfo slaveChannel = new ChannelInfo();

        DataLoggerSlaveChannelInfo info = new DataLoggerSlaveChannelInfoFactory().from(dataLoggerChannel, Optional.of(slaveChannel));

        assertThat(info.dataLoggerChannel).isEqualTo(dataLoggerChannel);
        assertThat(info.slaveChannel).isEqualTo(slaveChannel);
        assertThat(info.availabilityDate).isNull();
    }



}
