package com.energyict.protocols.impl.channels.serial.optical.rxtx;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.OpticalDriver;

import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.serial.direct.rxtx.RxTxSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides an implementation of a {@link ConnectionType} interface for optical
 * communication using the open source RxTX libraries.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:00
 */
public class RxTxOpticalConnectionType extends RxTxSerialConnectionType implements OpticalDriver {

    @Inject
    public RxTxOpticalConnectionType(@Named(ConnectionTypeServiceImpl.RXTX_PLAIN_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, PropertySpecService propertySpecService) {
        super(serialComponentService);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disonnect for this RxTxOpticalConnectionType
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 14:09:00 +0100 $";
    }

}