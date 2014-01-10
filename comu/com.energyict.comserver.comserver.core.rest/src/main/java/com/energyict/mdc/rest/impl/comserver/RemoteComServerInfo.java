package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.shadow.servers.RemoteComServerShadow;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RemoteComServerInfo extends InboundOutboundComServerInfo<RemoteComServerShadow> {

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

    public RemoteComServerShadow writeTo(RemoteComServerShadow comServerSource) {
        super.writeTo(comServerSource);
        comServerSource.setEventRegistrationUri(eventRegistrationUri);
        comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri);
        comServerSource.setOnlineComServerId(onlineComServerId);
        comServerSource.setQueryAPIPassword(queryAPIPassword);
        comServerSource.setQueryAPIUsername(queryAPIUsername);

        updateInboundComPorts(comServerSource);
        updateOutboundComPorts(comServerSource);

        return comServerSource;
    }

    public RemoteComServerShadow asShadow() {
        RemoteComServerShadow shadow = new RemoteComServerShadow();
        this.writeTo(shadow);
        return shadow;
    }

}
