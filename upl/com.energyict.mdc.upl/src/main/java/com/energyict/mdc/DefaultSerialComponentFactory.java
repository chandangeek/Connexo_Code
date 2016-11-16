package com.energyict.mdc;

import com.energyict.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
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
 * Provides a default implementation for the {@link SerialComponentFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-03 (17:58)
 */
public class DefaultSerialComponentFactory implements SerialComponentFactory {

    @Override
    public ServerSerialPort newSioSerialPort (SerialPortConfiguration configuration) {
        return new SioSerialPort(configuration);
    }

    @Override
    public ServerSerialPort newRxTxSerialPort (SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    @Override
    public SerialPortComChannel newSerialComChannel (ServerSerialPort serialPort) {
        return new SerialComChannel(serialPort);
    }

    @Override
    public AtModemComponent newAtModemComponent (AbstractAtModemProperties properties) {
        return new AtModemComponent(properties);
    }

    @Override
    public PaknetModemComponent newPaknetModemComponent(AbstractPaknetModemProperties properties) {
        return new PaknetModemComponent(properties);
    }

    @Override
    public PEMPModemComponent newPEMPModemComponent(AbstractPEMPModemProperties properties) {
        return new PEMPModemComponent(properties);
    }

    @Override
    public CaseModemComponent newCaseModemComponent(AbstractCaseModemProperties properties) {
        return new CaseModemComponent(properties);
    }
}