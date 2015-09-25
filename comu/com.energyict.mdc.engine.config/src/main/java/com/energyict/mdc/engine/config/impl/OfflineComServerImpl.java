package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.*;
import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.OfflineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:37)
 */
public class OfflineComServerImpl extends ComServerImpl implements OfflineComServer {

    @Inject
    public OfflineComServerImpl(DataModel dataModel, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
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