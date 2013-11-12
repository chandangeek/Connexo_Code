package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class OnlineComServerInfo extends ComServerInfo {
    public String queryAPIPostUri;
    public boolean usesDefaultQueryAPIPostUri;
    public String eventRegistrationUri;
    public boolean usesDefaultEventRegistrationUri;
    public int storeTaskQueueSize;
    public int numberOfStoreTaskThreads;
    public int storeTaskThreadPriority;

    public List<Object> comPorts;

    public OnlineComServerInfo() {
    }

    public OnlineComServerInfo(@Context final UriInfo uriInfo, final OnlineComServer onlineComServer) {
        super(onlineComServer);
        this.comServerDescriptor="OnlineComServer";
        this.queryAPIPostUri = onlineComServer.getQueryApiPostUri();
        this.usesDefaultQueryAPIPostUri = onlineComServer.usesDefaultQueryApiPostUri();
        this.eventRegistrationUri = onlineComServer.getEventRegistrationUri();
        this.usesDefaultEventRegistrationUri = onlineComServer.usesDefaultEventRegistrationUri();
        this.storeTaskQueueSize = onlineComServer.getStoreTaskQueueSize();
        this.numberOfStoreTaskThreads = onlineComServer.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = onlineComServer.getStoreTaskThreadPriority();
        comPorts = new ArrayList<>();
        for (final ComPort comPort : onlineComServer.getComPorts()) {
            comPorts.add(new Object() {
                public Integer id = comPort.getId();
                public URI href = uriInfo.getBaseUriBuilder().path(ComPortResource.class).path("/"+comPort.getId()).build();
            });
        }

    }

    public OnlineComServerShadow asShadow() {
        OnlineComServerShadow comServerShadow = super.asShadow();
        comServerShadow.setQueryAPIPostUri(queryAPIPostUri);
        comServerShadow.setUsesDefaultQueryAPIPostUri(usesDefaultQueryAPIPostUri);
        comServerShadow.setEventRegistrationUri(eventRegistrationUri);
        comServerShadow.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri);
        comServerShadow.setStoreTaskQueueSize(storeTaskQueueSize);
        comServerShadow.setStoreTaskThreadPriority(storeTaskThreadPriority);
        return comServerShadow;
    }

    @GET
    @Path("/comports")
    public List<Object> getComPorts() {
        return comPorts;
    }
}
