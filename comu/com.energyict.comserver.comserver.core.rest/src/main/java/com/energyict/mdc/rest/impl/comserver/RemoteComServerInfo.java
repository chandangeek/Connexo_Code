package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.*;

import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class RemoteComServerInfo extends ComServerInfo<RemoteComServer.RemoteComServerBuilder, RemoteComServer> {

    public RemoteComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public RemoteComServerInfo(final RemoteComServer remoteComServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService) {
        super(remoteComServer, comPorts, engineConfigurationService);
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
        this.eventRegistrationUri = remoteComServer.getEventRegistrationUri();
        this.usesDefaultEventRegistrationUri = remoteComServer.usesDefaultEventRegistrationUri();
        this.onlineComServerId = remoteComServer.getOnlineComServer() != null ? remoteComServer.getOnlineComServer().getId() : null;
    }

    public RemoteComServer.RemoteComServerBuilder writeTo(RemoteComServer.RemoteComServerBuilder comServerBuilder, EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerBuilder, engineConfigurationService);
        Optional<String> eventRegistrationUri = Optional.ofNullable(this.eventRegistrationUri);
        if (eventRegistrationUri.isPresent()) {
            comServerBuilder.eventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<Boolean> usesDefaultEventRegistrationUri = Optional.ofNullable(this.usesDefaultEventRegistrationUri);
        if (usesDefaultEventRegistrationUri.isPresent()) {
            comServerBuilder.usesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri.get());
        }
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
        Optional<String> eventRegistrationUri = Optional.ofNullable(this.eventRegistrationUri);
        if (eventRegistrationUri.isPresent()) {
            remoteComServer.setEventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<Boolean> usesDefaultEventRegistrationUri = Optional.ofNullable(this.usesDefaultEventRegistrationUri);
        if (usesDefaultEventRegistrationUri.isPresent()) {
            remoteComServer.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri.get());
        }
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
