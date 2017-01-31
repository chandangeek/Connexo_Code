/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.IPBasedInboundComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class TCPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements TCPBasedInboundComPort, IPBasedInboundComPort, ComPort, InboundComPort {

    @Inject
    protected TCPBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public boolean isTCPBased() {
        return true;
    }

    static class TCPBasedInboundComPortBuilderImpl
            extends IpBasedInboundComPortBuilderImpl<TCPBasedInboundComPortBuilder, TCPBasedInboundComPort>
            implements TCPBasedInboundComPortBuilder {

        protected TCPBasedInboundComPortBuilderImpl(TCPBasedInboundComPort ipBasedInboundComPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(TCPBasedInboundComPortBuilder.class, ipBasedInboundComPort, name, numberOfSimultaneousConnections, portNumber);
            comPort.setComPortType(ComPortType.TCP);
        }
    }
}