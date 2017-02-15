package com.energyict.mdc.channel.serial;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides functionality to manage a hardware serial(RS-232) port on the ComServer side
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/08/12
 * Time: 10:37
 */
public interface ServerSerialPort {

    /**
     * Opens a new SerialPort and initialize based on the configuration
     *
     * @throws com.energyict.mdc.upl.io.SerialPortException
     *          [serialPortDoesNotExist] - when the port does not exist on the ComServer
     * @throws com.energyict.mdc.upl.io.SerialPortException
     *          [serialPortIsInUse] - when the port is used by another process
     */
    void openAndInit();

    /**
     * Set the used inputStream for this ComPort
     *
     * @param inputStream the used inputStream
     */
    void setSerialInputStream(InputStream inputStream);

    /**
     * Set the used outputStream for this ComPort
     *
     * @param outputStream the used outputStream
     */
    void setSerialOutputStream(OutputStream outputStream);

    /**
     * Get the used inputStream for this ComPort
     *
     * @return the used inputStream
     */
    InputStream getInputStream();

    /**
     * Get the used outputStream for this ComPort
     *
     * @return the used outputStream
     */
    OutputStream getOutputStream();

    /**
     * Close the SerialPort so it can be used by other applications
     */
    void close();

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
     * Returns the used SignalController for this ServerSerialPort
     *
     * @return the used SignalController
     */
    SignalController getSerialPortSignalController();

    int txBufCount();

}
