package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class OnlineComServerInfo extends ComServerInfo {
    public String queryAPIPostUri;
    public boolean usesDefaultQueryAPIPostUri;
    public String eventRegistrationUri;
    public boolean usesDefaultEventRegistrationUri;
    public int storeTaskQueueSize;
    public int numberOfStoreTaskThreads;
    public int storeTaskThreadPriority;

    public List<Integer> comPorts_ids;

    public OnlineComServerInfo() {
    }

    public OnlineComServerInfo(OnlineComServer onlineComServer) {
        super(onlineComServer);
        this.comServerDescriptor="OnlineComServer";
        this.queryAPIPostUri = onlineComServer.getQueryApiPostUri();
        this.usesDefaultQueryAPIPostUri = onlineComServer.usesDefaultQueryApiPostUri();
        this.eventRegistrationUri = onlineComServer.getEventRegistrationUri();
        this.usesDefaultEventRegistrationUri = onlineComServer.usesDefaultEventRegistrationUri();
        this.storeTaskQueueSize = onlineComServer.getStoreTaskQueueSize();
        this.numberOfStoreTaskThreads = onlineComServer.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = onlineComServer.getStoreTaskThreadPriority();
        comPorts_ids = new ArrayList<>();
        for (ComPort comPort : onlineComServer.getComPorts()) {
            comPorts_ids.add(comPort.getId());
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
    public List<Integer> getComPorts_ids() {
        return comPorts_ids;
    }
}
