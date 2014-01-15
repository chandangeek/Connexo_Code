package com.energyict.protocols.mdc.channels.serial;

import com.energyict.protocols.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.protocols.mdc.channels.serial.modem.AbstractAtModemProperties;
import com.energyict.protocols.mdc.channels.serial.modem.AbstractCaseModemProperties;
import com.energyict.protocols.mdc.channels.serial.modem.AbstractPEMPModemProperties;
import com.energyict.protocols.mdc.channels.serial.modem.AbstractPaknetModemProperties;
import com.energyict.protocols.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.PEMPModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.PaknetModemComponent;

/**
 * Provides a default implementation for the {@link SerialComponentFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-03 (17:58)
 */
public class DefaultSerialComponentFactory implements SerialComponentFactory {

    @Override
    public SioSerialPort newSioSerialPort (SerialPortConfiguration configuration) {
        return new SioSerialPort(configuration);
    }

    @Override
    public RxTxSerialPort newRxTxSerialPort (SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    @Override
    public SerialComChannel newSerialComChannel (ServerSerialPort serialPort) {
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