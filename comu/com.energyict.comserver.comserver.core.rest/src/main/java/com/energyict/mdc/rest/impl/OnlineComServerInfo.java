package com.energyict.mdc.rest.impl;

import com.energyict.mdc.servers.OnlineComServer;

public class OnlineComServerInfo extends ComServerInfo{
    private String queryAPIPostUri;
    private boolean usesDefaultQueryAPIPostUri;
    private String eventRegistrationUri;
    private boolean usesDefaultEventRegistrationUri;
    private int storeTaskQueueSize;
    private int numberOfStoreTaskThreads;
    private int storeTaskThreadPriority;

    public OnlineComServerInfo(OnlineComServer onlineComServer) {
        super("OnlineComServer");
        this.queryAPIPostUri = onlineComServer.getQueryApiPostUri();
    }


}
