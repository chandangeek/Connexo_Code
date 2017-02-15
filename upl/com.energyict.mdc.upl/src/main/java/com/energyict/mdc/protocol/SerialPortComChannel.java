package com.energyict.mdc.protocol;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;

/**
 * Implementers of this interface allow protocols to change the baud rate and the line control parameters of this ComChannel.
 * This is e.g. necessary for doing an IEC1107 HHU sign on.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/11/13
 * Time: 13:56
 * Author: khe
 */
public interface SerialPortComChannel extends ComChannel {

    /**
     * Update the serialPort configuration based on the given {@link SerialPortConfiguration}
     *
     * @param serialPortConfiguration the new configuration to set on the device
     */
    void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration);

    /**
     * Get the current {@link SerialPortConfiguration}
     *
     * @return the used serialPortConfiguration
     */
    SerialPortConfiguration getSerialPortConfiguration();

    /**
     * @return the serialport for this ComChannel
     */
    ServerSerialPort getSerialPort();
}
