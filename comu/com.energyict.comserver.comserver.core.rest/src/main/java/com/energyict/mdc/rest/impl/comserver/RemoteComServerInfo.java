package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.*;
import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class RemoteComServerInfo extends ComServerInfo<RemoteComServer> {

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
        this.onlineComServerId = remoteComServer.getOnlineComServer()!=null?remoteComServer.getOnlineComServer().getId():null;
    }

    public RemoteComServer writeTo(RemoteComServer comServerSource,EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerSource, engineConfigurationService);
        Optional<String> eventRegistrationUri = Optional.ofNullable(this.eventRegistrationUri);
        if(eventRegistrationUri.isPresent()) {
            comServerSource.setEventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<Boolean> usesDefaultEventRegistrationUri = Optional.ofNullable(this.usesDefaultEventRegistrationUri);
        if(usesDefaultEventRegistrationUri.isPresent()) {
            comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri.get());
        }
        Optional<Long> onlineComServerId = Optional.ofNullable(this.onlineComServerId);
        if(onlineComServerId.isPresent()) {
            Optional<? extends ComServer> onlineComServer = engineConfigurationService.findComServer(onlineComServerId.get());
            if(onlineComServer.isPresent() && OnlineComServer.class.isAssignableFrom(onlineComServer.get().getClass())) {
                comServerSource.setOnlineComServer((OnlineComServer)onlineComServer.get());
            }
        }

        return comServerSource;
    }

    @Override
    protected RemoteComServer createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newRemoteComServerInstance();
    }
}
