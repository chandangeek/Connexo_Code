/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;

import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.OfflineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:37)
 */
public final class OfflineComServerImpl extends ComServerImpl implements OfflineComServer {

    @Inject
    public OfflineComServerImpl(DataModel dataModel, Provider<OutboundComPort> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Override
    public boolean isOffline() {
        return true;
    }

    static class OfflineComServerBuilderImpl extends AbstractComServerBuilder<OfflineComServerImpl, OfflineComServerBuilderImpl> {

        @Inject
        public OfflineComServerBuilderImpl(DataModel dataModel) {
            super(dataModel.getInstance(OfflineComServerImpl.class), OfflineComServerBuilderImpl.class);
        }

    }

}