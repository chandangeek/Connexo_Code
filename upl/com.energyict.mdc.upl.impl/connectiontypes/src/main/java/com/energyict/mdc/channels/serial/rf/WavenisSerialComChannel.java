package com.energyict.mdc.channels.serial.rf;

import com.energyict.mdc.channels.SynchroneousComChannel;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.protocol.SerialPortComChannel;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Comchannel that represents the link to an RF Wavenis module over a serial port.
 * This is meant for usage with Waveport devices connected via USB.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 10:52
 * Author: khe
 */
public class WavenisSerialComChannel extends SynchroneousComChannel implements SerialPortComChannel {

    ServerSerialPort serialPort;

    public WavenisSerialComChannel(InputStream inputStream, OutputStream outputStream, ServerSerialPort serialPort) {
        super(inputStream, outputStream);
        this.serialPort = serialPort;
    }

    @Override
    public ServerSerialPort getSerialPort() {
        return serialPort;
    }

    @Override
    public SerialPortConfiguration getSerialPortConfiguration() {
        return getSerialPort().getSerialPortConfiguration();
    }

    @Override
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        getSerialPort().updatePortConfiguration(serialPortConfiguration);
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
}
