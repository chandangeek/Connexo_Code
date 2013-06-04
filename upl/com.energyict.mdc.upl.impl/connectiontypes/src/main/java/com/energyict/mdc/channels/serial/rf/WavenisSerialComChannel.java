package com.energyict.mdc.channels.serial.rf;

import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.protocol.SynchroneousComChannel;

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
public class WavenisSerialComChannel extends SynchroneousComChannel {

    ServerSerialPort serialPort;

    public WavenisSerialComChannel(InputStream inputStream, OutputStream outputStream, ServerSerialPort serialPort) {
        super(inputStream, outputStream);
        this.serialPort = serialPort;
    }

    /**
     * Close the serial port
     */
    @Override
    public void close() {
        try {
            super.close();
        } finally {
            if (this.serialPort != null) {
                this.serialPort.close();
            }
        }
    }
}
