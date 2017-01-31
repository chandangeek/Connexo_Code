/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.OpticalComChannel;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;

public class OpticalComChannelImpl extends SynchronousComChannel implements OpticalComChannel {

    private final SerialComChannel serialComChannel;

    public OpticalComChannelImpl(SerialComChannel serialComChannel) {
        super(serialComChannel.getSerialPort().getInputStream(), serialComChannel.getSerialPort().getOutputStream());
        this.serialComChannel = serialComChannel;
    }

    @Override
    public ServerSerialPort getSerialPort() {
        return serialComChannel.getSerialPort();
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.OPTICAL_COM_CHANNEL;
    }
}
