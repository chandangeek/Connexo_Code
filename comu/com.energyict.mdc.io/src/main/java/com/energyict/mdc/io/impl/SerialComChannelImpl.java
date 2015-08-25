package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;

/**
 * Creates a simple {@link ComChannel} wrapped around a {@link ServerSerialPort}.
 * <p>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 9:34
 */
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