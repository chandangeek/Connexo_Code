/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.config.*;

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.OfflineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:37)
 */
@XmlRootElement
public final class OfflineComServerImpl extends ComServerImpl implements OfflineComServer {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
    private final Reference<OnlineComServer> onlineComServer = ValueReference.absent();

    /**
     * Fixed URL, only used in detached mode (localhost, offline)
     */
    public static final String EVENT_REGISTRATION_URI = "ws://localhost:8090/events/registration";

    private String eventRegistrationUriIfSupported;
    private String queryApiPostUriIfSupported;

    protected OfflineComServerImpl() {
        super();
    }

    @Inject
    public OfflineComServerImpl(DataModel dataModel, Provider<OutboundComPort> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Override
    @XmlElement
    public String getEventRegistrationUriIfSupported () {
        return EVENT_REGISTRATION_URI;
    }

    @Override
    @XmlElement
    public String getQueryApiPostUriIfSupported () {
        return null;
    }

    @Override
    public boolean isOffline() {
        return true;
    }

    static class OfflineComServerBuilderImpl extends AbstractComServerBuilder<OfflineComServerImpl, OfflineComServerBuilder> implements OfflineComServerBuilder<OfflineComServerImpl> {

        @Inject
        public OfflineComServerBuilderImpl(DataModel dataModel) {
            super(dataModel.getInstance(OfflineComServerImpl.class), OfflineComServerBuilder.class);
        }

        @Override
        public OfflineComServerBuilder onlineComServer(OnlineComServer onlineComServer) {
            getComServerInstance().setOnlineComServer(onlineComServer);
            return this;
        }

        @Override
        public OfflineComServerBuilder serverMonitorUrl(String serverUrl) {
            getComServerInstance().setServerMonitorUrl(serverUrl);
            return this;
        }
    }

    @Override
    @XmlElement(type = OnlineComServerImpl.class)
    @XmlElementWrapper
    public OnlineComServer getOnlineComServer() {
        return this.onlineComServer.get();
    }

    @Override
    public void setOnlineComServer(OnlineComServer onlineComServer) {
        this.onlineComServer.set(onlineComServer);
    }

    @Override
    public List<OutboundComPort> getOutboundComPorts() {
        return super.getOutboundComPorts();
    }
}