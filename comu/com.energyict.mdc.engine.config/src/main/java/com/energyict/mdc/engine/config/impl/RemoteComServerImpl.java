/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the {@link RemoteComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:40)
 */
@XmlRootElement
public final class RemoteComServerImpl extends ComServerImpl implements RemoteComServer {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
    private final Reference<OnlineComServer> onlineComServer = ValueReference.absent();

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String serverName;
    @Range(min = MIN_REQUIRED_PORT_RANGE, max = MAX_PORT_RANGE, message = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", groups = {Save.Update.class, Save.Create.class})
    private int eventRegistrationPort;
    @Range(min = MIN_REQUIRED_PORT_RANGE, max = MAX_PORT_RANGE, message = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", groups = {Save.Update.class, Save.Create.class})
    private int statusPort;

    public static RemoteComServer from(DataModel dataModel) {
        return dataModel.getInstance(RemoteComServerImpl.class);
    }

    @Inject
    public RemoteComServerImpl(DataModel dataModel, Provider<OutboundComPort> outboundComPortProvider, Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider, Provider<CoapBasedInboundComPort> coapBasedInboundComPortProvider, Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider, Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider, Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider, Thesaurus thesaurus) {
        super(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public OnlineComServer getOnlineComServer() {
        return this.onlineComServer.get();
    }

    @Override
    public void setOnlineComServer(OnlineComServer onlineComServer) {
        this.onlineComServer.set(onlineComServer);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int getEventRegistrationPort() {
        return this.eventRegistrationPort;
    }

    @Override
    public void setEventRegistrationPort(int eventRegistrationPort) {
        this.eventRegistrationPort = eventRegistrationPort;
    }

    @Override
    @XmlElement
    public String getEventRegistrationUri() {
        return eventRegistrationPort != 0 ? buildEventRegistrationUri(getServerName(), getEventRegistrationPort()) : "";
    }

    @Override
    public String getEventRegistrationUriIfSupported() {
        if (Checks.is(this.getEventRegistrationUri()).emptyOrOnlyWhiteSpace()) {
            return super.getEventRegistrationUriIfSupported();
        } else {
            return this.getEventRegistrationUri();
        }
    }

    @Override
    public int getStatusPort() {
        return this.statusPort;
    }

    @Override
    public void setStatusPort(int statusPort) {
        this.statusPort = statusPort;
    }

    @Override
    public List<InboundComPort> getInboundComPorts () {
        return super.getInboundComPorts();
    }

    @Override
    public List<OutboundComPort> getOutboundComPorts () {
        return super.getOutboundComPorts();
    }

    @Override
    @XmlElement
    public String getStatusUri() {
        return buildStatusUri(getServerName(), getStatusPort());
    }

    @Override
    public boolean supportsExecutionOfHighPriorityComTasks() {
        return true;
    }

    public static class RemoteComServerBuilderImpl extends AbstractComServerBuilder<RemoteComServerImpl, RemoteComServerBuilder> implements RemoteComServerBuilder<RemoteComServerImpl> {

        @Inject
        RemoteComServerBuilderImpl(DataModel dataModel) {
            super(dataModel.getInstance(RemoteComServerImpl.class), RemoteComServerBuilder.class);
        }

        @Override
        public RemoteComServerBuilder onlineComServer(OnlineComServer onlineComServer) {
            getComServerInstance().setOnlineComServer(onlineComServer);
            return this;
        }

        @Override
        public RemoteComServerBuilder serverName(String serverName) {
            getComServerInstance().setServerName(serverName);
            return this;
        }

        @Override
        public RemoteComServerBuilder serverMonitorUrl(String serverUrl) {
            getComServerInstance().setServerMonitorUrl(serverUrl);
            return this;
        }

        @Override
        public RemoteComServerBuilder eventRegistrationPort(int eventRegistrationPort) {
            getComServerInstance().setEventRegistrationPort(eventRegistrationPort);
            return this;
        }

        @Override
        public RemoteComServerBuilder statusPort(int statusPort) {
            getComServerInstance().setStatusPort(statusPort);
            return this;
        }
    }

}