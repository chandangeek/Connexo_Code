/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.optical.serialio;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.OpticalDriver;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class SioOpticalConnectionType extends SioSerialConnectionType implements OpticalDriver {

    @Inject
    public SioOpticalConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PLAIN_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(ConnectionTypeServiceImpl.SERIAL_PLAIN_GUICE_INJECTION_NAME, serialComponentService, thesaurus);
    }

    @Override
    public SerialComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return getSerialComponentService().createOpticalFromSerialComChannel(super.connect(properties));
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