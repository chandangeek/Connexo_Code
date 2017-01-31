/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.direct.rxtx;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

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