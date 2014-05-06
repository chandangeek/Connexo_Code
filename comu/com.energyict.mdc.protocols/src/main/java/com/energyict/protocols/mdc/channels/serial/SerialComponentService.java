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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides factory services for serial IO components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-03 (17:56)
 */
public interface SerialComponentService {

    public SioSerialPort newSioSerialPort (SerialPortConfiguration configuration);

    public RxTxSerialPort newRxTxSerialPort (SerialPortConfiguration configuration);

    public SerialComChannel newSerialComChannel (ServerSerialPort serialPort);

    public AtModemComponent newAtModemComponent (AbstractAtModemProperties properties);

    public PaknetModemComponent newPaknetModemComponent (AbstractPaknetModemProperties properties);

    public PEMPModemComponent newPEMPModemComponent (AbstractPEMPModemProperties properties);

    public CaseModemComponent newCaseModemComponent (AbstractCaseModemProperties properties);

}