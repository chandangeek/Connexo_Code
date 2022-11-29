/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.IPBasedInboundComPort;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPInboundComPort;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPBasedInboundComPortImpl extends UDPInboundComPortImpl implements UDPBasedInboundComPort, UDPInboundComPort, IPBasedInboundComPort, OutboundComPort, InboundComPort {

    @Inject
    protected UDPBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    static class UDPBasedInboundComPortBuilderImpl
            extends UDPInboundComPortBuilderImpl<UDPBasedInboundComPortBuilder, UDPBasedInboundComPort>
            implements UDPBasedInboundComPortBuilder {

        protected UDPBasedInboundComPortBuilderImpl(UDPBasedInboundComPort udpBasedInboundComPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(UDPBasedInboundComPortBuilder.class, udpBasedInboundComPort, name, numberOfSimultaneousConnections, portNumber);
        }
    }
}