package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class RemoteComServerInfo extends InboundOutboundComServerInfo<RemoteComServer> {

    public RemoteComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public RemoteComServerInfo(final RemoteComServer remoteComServer, List<ComPort> comPorts) {
        super(remoteComServer, comPorts);
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
        this.queryAPIUsername = remoteComServer.getQueryAPIUsername();
        this.queryAPIPassword = remoteComServer.getQueryAPIPassword();
    }

    public RemoteComServer writeTo(RemoteComServer comServerSource,EngineModelService engineModelService) {
        super.writeTo(comServerSource,engineModelService);
        comServerSource.setEventRegistrationUri(eventRegistrationUri);
        comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri);
        comServerSource.setOnlineComServer((OnlineComServer) engineModelService.findComServer(onlineComServerId));
        comServerSource.setQueryAPIPassword(queryAPIPassword);
        comServerSource.setQueryAPIUsername(queryAPIUsername);

        updateInboundComPorts(comServerSource,engineModelService);
        updateOutboundComPorts(comServerSource,engineModelService);

        return comServerSource;
    }
}
