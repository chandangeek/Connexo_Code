/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;

public class SerialComChannelImpl extends SynchronousComChannel implements SerialComChannel {

    private final ServerSerialPort serialPort;

    public SerialComChannelImpl(ServerSerialPort serialPort) {
        super(serialPort.getInputStream(), serialPort.getOutputStream());
        this.serialPort = serialPort;
    }

    @Override
    protected void doClose() {
        try {
            super.doClose();
        }
        finally {
            this.serialPort.close();
        }
    }

    @Override
    public ServerSerialPort getSerialPort() {
        return this.serialPort;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.SERIAL_COM_CHANNEL;
    }
}