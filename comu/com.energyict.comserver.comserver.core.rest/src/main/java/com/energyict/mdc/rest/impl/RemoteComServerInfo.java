package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.RemoteComServer;
import com.energyict.mdc.shadow.servers.RemoteComServerShadow;
import java.util.List;

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

    public RemoteComServerShadow writeToShadow(RemoteComServerShadow comServerShadow) {
        super.writeToShadow(comServerShadow);
        comServerShadow.setEventRegistrationUri(eventRegistrationUri);
        comServerShadow.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri);
        comServerShadow.setOnlineComServerId(onlineComServerId);
        comServerShadow.setQueryAPIPassword(queryAPIPassword);
        comServerShadow.setQueryAPIUsername(queryAPIUsername);

        updateInboundComPorts(comServerShadow);
        updateOutboundComPorts(comServerShadow);

        return comServerShadow;
    }

    public RemoteComServerShadow asShadow() {
        RemoteComServerShadow shadow = new RemoteComServerShadow();
        this.writeToShadow(shadow);
        return shadow;
    }

}
