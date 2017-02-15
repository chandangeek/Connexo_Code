package com.energyict.mdc.channel.serial;

import com.energyict.mdc.channel.SynchroneousComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;

/**
 * Creates a simple {@link com.energyict.mdc.protocol.ComChannel} wrapped around a {@link ServerSerialPort}.
 * <p>
 * Implementing the {@link SerialPortComChannel} interface allows protocols to change the baud rate and the line control parameters.
 * This is e.g. necessary for doing an IEC1107 HHU sign on.
 * <p>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 9:34
 */
public class SerialComChannelImpl extends SynchroneousComChannel implements SerialPortComChannel {

    private final ServerSerialPort serialPort;
    private final ComChannelType comChannelType;

    public SerialComChannelImpl(ServerSerialPort serialPort, ComChannelType comChannelType) {
        super(serialPort.getInputStream(), serialPort.getOutputStream());
        this.serialPort = serialPort;
        this.comChannelType = comChannelType;
    }

    @Override
    public ComChannelType getComChannelType() {
        return comChannelType;
    }

    @Override
    public void doClose() {
        try {
            super.doClose();
        } finally {
            if (this.serialPort != null) {
                this.serialPort.close();
            }
        }
    }

    @Override
    public ServerSerialPort getSerialPort() {
        return this.serialPort;
    }

    @Override
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        getSerialPort().updatePortConfiguration(serialPortConfiguration);
    }

    @Override
    public SerialPortConfiguration getSerialPortConfiguration() {
        return getSerialPort().getSerialPortConfiguration();
    }
}