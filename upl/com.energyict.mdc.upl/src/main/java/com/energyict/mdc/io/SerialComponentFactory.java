package com.energyict.mdc.io;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractAtModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractCaseModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractPEMPModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractPaknetModemProperties;
import com.energyict.mdc.channels.serial.modem.postdialcommand.ModemComponent;
import com.energyict.mdc.protocol.SerialPortComChannel;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides factory services for serial IO components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-03 (17:56)
 */
public interface SerialComponentFactory {

    /**
     * Holds the instance for the SerialComponentFactory.
     * Users of this library should provide a value according to their own specifications
     */
    AtomicReference<SerialComponentFactory> instance = new AtomicReference<>();

    public ServerSerialPort newSioSerialPort(SerialPortConfiguration configuration);

    public ServerSerialPort newRxTxSerialPort(SerialPortConfiguration configuration);

    public SerialPortComChannel newSerialComChannel(ServerSerialPort serialPort);

    public ModemComponent newAtModemComponent(AbstractAtModemProperties properties);

    public ModemComponent newPaknetModemComponent(AbstractPaknetModemProperties properties);

    public ModemComponent newPEMPModemComponent(AbstractPEMPModemProperties properties);

    public ModemComponent newCaseModemComponent(AbstractCaseModemProperties properties);

}