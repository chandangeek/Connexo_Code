package com.energyict.protocols.impl.channels.serial.optical.serialio;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.OpticalDriver;

import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides an implementation of a {@link ConnectionType} interface for optical
 * communication using the SerialIO libraries
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 12:58
 */
public class SioOpticalConnectionType extends SioSerialConnectionType implements OpticalDriver {

    @Inject
    public SioOpticalConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PLAIN_GUICE_INJECTION_NAME) SerialComponentService serialComponentService) {
        super(serialComponentService);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this SioOpticalConnectionType
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 14:09:00 +0100 $";
    }

}