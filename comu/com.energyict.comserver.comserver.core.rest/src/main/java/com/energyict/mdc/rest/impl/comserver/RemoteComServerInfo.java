package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.*;
import com.google.common.base.Optional;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class RemoteComServerInfo extends ComServerInfo<RemoteComServer> {

    public RemoteComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public RemoteComServerInfo(final RemoteComServer remoteComServer, List<ComPort> comPorts, EngineModelService engineModelService) {
        super(remoteComServer, comPorts, engineModelService);
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

    public RemoteComServer writeTo(RemoteComServer comServerSource,EngineModelService engineModelService) {
        super.writeTo(comServerSource,engineModelService);
        Optional<String> eventRegistrationUri = Optional.fromNullable(this.eventRegistrationUri);
        if(eventRegistrationUri.isPresent()) {
            comServerSource.setEventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<Boolean> usesDefaultEventRegistrationUri = Optional.fromNullable(this.usesDefaultEventRegistrationUri);
        if(usesDefaultEventRegistrationUri.isPresent()) {
            comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri.get());
        }
        Optional<Long> onlineComServerId = Optional.fromNullable(this.onlineComServerId);
        if(onlineComServerId.isPresent()) {
            Optional<? extends ComServer> onlineComServer = engineModelService.findComServer(onlineComServerId.get());
            if(onlineComServer.isPresent() && OnlineComServer.class.isAssignableFrom(onlineComServer.get().getClass())) {
                comServerSource.setOnlineComServer((OnlineComServer)onlineComServer.get());
            }
        }

        return comServerSource;
    }

    @Override
    protected RemoteComServer createNew(EngineModelService engineModelService) {
        return engineModelService.newRemoteComServerInstance();
    }
}
