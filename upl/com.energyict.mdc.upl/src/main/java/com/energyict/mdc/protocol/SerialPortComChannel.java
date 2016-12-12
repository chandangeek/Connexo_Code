package com.energyict.mdc.protocol;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;

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
     * Update the serialPort configuration based on the given {@link com.energyict.mdc.channels.serial.SerialPortConfiguration}
     *
     * @param serialPortConfiguration the new configuration to set on the device
     */
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration);

    /**
     * Get the current {@link SerialPortConfiguration}
     *
     * @return the used serialPortConfiguration
     */
    public SerialPortConfiguration getSerialPortConfiguration();

    /**
     * @return the serialport for this ComChannel
     */
    public ServerSerialPort getSerialPort();
}
