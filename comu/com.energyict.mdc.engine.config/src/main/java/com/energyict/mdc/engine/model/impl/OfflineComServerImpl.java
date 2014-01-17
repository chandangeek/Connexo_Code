package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.google.inject.Provider;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OfflineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:37)
 */
public class OfflineComServerImpl extends ComServerImpl implements ServerOfflineComServer {

    @Inject
    public OfflineComServerImpl(DataModel dataModel, EngineModelService engineModelService, Provider<OutboundComPortImpl> outboundComPortProvider, Provider<ServerServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ServerModemBasedInboundComPort> modemBasedInboundComPortProvider) {
        super(dataModel, engineModelService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider);
    }

    @Override
    public String getType () {
        return OfflineComServer.class.getName();
    }

    @Override
    public boolean isOffline () {
        return true;
    }

}