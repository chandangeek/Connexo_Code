/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public class RemoteComServerInfo extends ComServerInfo<RemoteComServer.RemoteComServerBuilder, RemoteComServer> {

    public RemoteComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public RemoteComServerInfo(final RemoteComServer remoteComServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService, ComPortInfoFactory comPortInfoFactory) {
        super(remoteComServer, comPorts, engineConfigurationService, comPortInfoFactory);
        readFrom(remoteComServer);
    }

    /**
     * Create Info based solely on comserver properties without comports
     */
    public RemoteComServerInfo(final RemoteComServer remoteComServer) {
        super(remoteComServer);
        readFrom(remoteComServer);
    }

    private void readFrom(RemoteComServer remoteComServer) {
        this.serverName = remoteComServer.getServerName();
        this.eventRegistrationPort = remoteComServer.getEventRegistrationPort();
        this.statusPort = remoteComServer.getStatusPort();
        this.onlineComServerId = remoteComServer.getOnlineComServer() != null ? remoteComServer.getOnlineComServer().getId() : null;
    }

    public RemoteComServer.RemoteComServerBuilder writeTo(RemoteComServer.RemoteComServerBuilder comServerBuilder, EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerBuilder, engineConfigurationService);
        comServerBuilder.serverName(this.serverName);
        comServerBuilder.statusPort(this.statusPort != null ? this.statusPort : 0);
        comServerBuilder.eventRegistrationPort(this.eventRegistrationPort != null ? this.eventRegistrationPort : 0);
        Optional<Long> onlineComServerId = Optional.ofNullable(this.onlineComServerId);
        if (onlineComServerId.isPresent()) {
            Optional<? extends ComServer> onlineComServer = engineConfigurationService.findComServer(onlineComServerId.get());
            if (onlineComServer.isPresent() && OnlineComServer.class.isAssignableFrom(onlineComServer.get().getClass())) {
                comServerBuilder.onlineComServer((OnlineComServer) onlineComServer.get());
            }
        }

        return comServerBuilder;
    }

    @Override
    public RemoteComServer updateTo(RemoteComServer remoteComServer, EngineConfigurationService engineConfigurationService) {
        remoteComServer.setServerName(this.serverName);
        remoteComServer.setStatusPort(this.statusPort != null ? this.statusPort : 0);
        remoteComServer.setEventRegistrationPort(this.eventRegistrationPort != null ? this.eventRegistrationPort : 0);
        Optional<Long> onlineComServerId = Optional.ofNullable(this.onlineComServerId);
        if (onlineComServerId.isPresent()) {
            Optional<? extends ComServer> onlineComServer = engineConfigurationService.findComServer(onlineComServerId.get());
            if (onlineComServer.isPresent() && OnlineComServer.class.isAssignableFrom(onlineComServer.get().getClass())) {
                remoteComServer.setOnlineComServer((OnlineComServer) onlineComServer.get());
            }
        }
        super.updateTo(remoteComServer, engineConfigurationService);
        return remoteComServer;
    }

    @Override
    protected RemoteComServer.RemoteComServerBuilder createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newRemoteComServerBuilder();
    }
}
