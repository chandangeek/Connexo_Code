package com.energyict.mdc;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractAtModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractCaseModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractPEMPModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractPaknetModemProperties;
import com.energyict.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.PEMPModemComponent;
import com.energyict.mdc.channels.serial.modem.PaknetModemComponent;
import com.energyict.mdc.protocol.SerialPortComChannel;

/**
 * Provides factory services for serial IO components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-03 (17:56)
 */
public interface SerialComponentFactory {

    public ServerSerialPort newSioSerialPort (SerialPortConfiguration configuration);

    public ServerSerialPort newRxTxSerialPort (SerialPortConfiguration configuration);

    public SerialPortComChannel newSerialComChannel (ServerSerialPort serialPort);

    public AtModemComponent newAtModemComponent (AbstractAtModemProperties properties);

    public PaknetModemComponent newPaknetModemComponent (AbstractPaknetModemProperties properties);

    public PEMPModemComponent newPEMPModemComponent (AbstractPEMPModemProperties properties);

    public CaseModemComponent newCaseModemComponent (AbstractCaseModemProperties properties);

}