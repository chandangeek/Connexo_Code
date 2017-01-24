package com.energyict.protocols.impl.channels.serial.direct.rxtx;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 11:14
 */
public class RxTxPlainSerialConnectionType extends RxTxSerialConnectionType {

    @Inject
    public RxTxPlainSerialConnectionType(@Named(ConnectionTypeServiceImpl.RXTX_PLAIN_GUICE_INJECTION_NAME) SerialComponentService plainRxTxSerialComponentService, Thesaurus thesaurus) {
        super(ConnectionTypeServiceImpl.RXTX_PLAIN_GUICE_INJECTION_NAME, plainRxTxSerialComponentService, thesaurus);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this RxTxSerialConnectionType
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 11:54:41 +0100$";
    }

}