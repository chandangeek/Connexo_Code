package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.inject.Provider;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class TCPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements TCPBasedInboundComPort, IPBasedInboundComPort, ComPort, InboundComPort {

    @Inject
    protected TCPBasedInboundComPortImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public boolean isTCPBased() {
        return true;
    }

    static class TCPBasedInboundComPortBuilderImpl
            extends IpBasedInboundComPortBuilderImpl<TCPBasedInboundComPortBuilder, TCPBasedInboundComPort>
            implements TCPBasedInboundComPortBuilder {

        protected TCPBasedInboundComPortBuilderImpl(Provider<TCPBasedInboundComPort> ipBasedInboundComPortProvider) {
            super(TCPBasedInboundComPortBuilder.class, ipBasedInboundComPortProvider);
            comPort.setComPortType(ComPortType.TCP);
        }
    }
}