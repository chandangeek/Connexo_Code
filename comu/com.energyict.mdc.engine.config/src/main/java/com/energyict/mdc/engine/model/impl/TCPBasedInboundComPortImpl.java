package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.google.inject.Provider;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class TCPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerTCPBasedInboundComPort {

    @Inject
    protected TCPBasedInboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMemberImpl> comPortPoolMemberProvider) {
        super(dataModel, comPortPoolMemberProvider);
    }

    @Override
    public boolean isTCPBased() {
        return true;
    }

    static class TCPBasedInboundComPortBuilderImpl
            extends IpBasedInboundComPortBuilderImpl<TCPBasedInboundComPortBuilder, ServerTCPBasedInboundComPort>
            implements TCPBasedInboundComPortBuilder {

        protected TCPBasedInboundComPortBuilderImpl(Provider<ServerTCPBasedInboundComPort> ipBasedInboundComPortProvider) {
            super(TCPBasedInboundComPortBuilder.class, ipBasedInboundComPortProvider);
        }
    }
}