package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.OpticalComChannel;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;

/**
 * Serves as an Optical ComChannel
 *
 * Copyrights EnergyICT
 * Date: 12/11/14
 * Time: 3:22 PM
 */
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
