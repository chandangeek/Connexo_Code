package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides functionality to manage a hardware serial(RS-232) port on the ComServer side.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/08/12
 * Time: 10:37
 */
@ProviderType
public interface ServerSerialPort {

    /**
     * Opens a new SerialPort and initializes it from the specified configuration.
     *
     * @throws SerialPortException
     *          [serialPortDoesNotExist] - when the port does not exist on the ComServer
     *          [serialPortIsInUse] - when the port is used by another process
     */
    public void openAndInit();

    /**
     * Sets the used inputStream for this ComPort.
     *
     * @param inputStream the used inputStream
     */
    public void setSerialInputStream(InputStream inputStream);

    /**
     * Sets the used outputStream for this ComPort.
     *
     * @param outputStream the used outputStream
     */
    public void setSerialOutputStream(OutputStream outputStream);

    /**
     * Gets the used inputStream for this ComPort.
     *
     * @return the used inputStream
     */
    public InputStream getInputStream();

    /**
     * Gets the used outputStream for this ComPort.
     *
     * @return the used outputStream
     */
    public OutputStream getOutputStream();

    /**
     * Closes the SerialPort so it can be used by other applications.
     */
    public void close();

    /**
     * Updates the serialPort configuration based on the given {@link SerialPortConfiguration}.
     *
     * @param serialPortConfiguration the new configuration to set on the device
     */
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration);

    /**
     * Gets the current {@link SerialPortConfiguration}.
     *
     * @return the used serialPortConfiguration
     */
    public SerialPortConfiguration getSerialPortConfiguration();

    /**
     * Gets the used SignalController for this ServerSerialPort.
     *
     * @return the used SignalController
     */
    public SignalController getSerialPortSignalController();

}
