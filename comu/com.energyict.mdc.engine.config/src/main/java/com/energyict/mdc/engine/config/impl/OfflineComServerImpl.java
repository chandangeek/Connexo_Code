/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;

import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link OfflineComServer} interface.
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

    @Override
    public boolean supportsExecutionOfHighPriorityComTasks() {
        return false;
    }

    public static class OfflineComServerBuilderImpl extends AbstractComServerBuilder<OfflineComServerImpl, OfflineComServerBuilder> implements OfflineComServerBuilder<OfflineComServerImpl> {

        @Inject
        public OfflineComServerBuilderImpl(DataModel dataModel) {
            super(dataModel.getInstance(OfflineComServerImpl.class), OfflineComServerBuilder.class);
        }

        @Override
        public OfflineComServerBuilder serverMonitorUrl(String serverUrl) {
            getComServerInstance().setServerMonitorUrl(serverUrl);
            return this;
        }
    }

}