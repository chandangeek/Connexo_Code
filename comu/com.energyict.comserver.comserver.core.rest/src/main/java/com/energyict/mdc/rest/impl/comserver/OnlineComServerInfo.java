/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
public class OnlineComServerInfo extends ComServerInfo<OnlineComServer.OnlineComServerBuilder, OnlineComServer> {

    public OnlineComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public OnlineComServerInfo(final OnlineComServer onlineComServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService, ComPortInfoFactory comPortInfoFactory) {
        super(onlineComServer, comPorts, engineConfigurationService, comPortInfoFactory);
        readFrom(onlineComServer);
    }

    /**
     * Create Info based solely on comserver properties without comports
     */
    public OnlineComServerInfo(final OnlineComServer onlineComServer) {
        super(onlineComServer);
        readFrom(onlineComServer);
    }

    private void readFrom(OnlineComServer onlineComServer) {
        this.serverName = onlineComServer.getServerName();
        this.queryAPIPort = onlineComServer.getQueryApiPort();
        this.eventRegistrationPort = onlineComServer.getEventRegistrationPort();
        this.statusPort = onlineComServer.getStatusPort();
        this.storeTaskQueueSize = onlineComServer.getStoreTaskQueueSize();
        this.numberOfStoreTaskThreads = onlineComServer.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = onlineComServer.getStoreTaskThreadPriority();
    }

    public OnlineComServer.OnlineComServerBuilder writeTo(OnlineComServer.OnlineComServerBuilder comServerBuilder, EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerBuilder, engineConfigurationService);
        comServerBuilder.serverName(this.serverName);
        comServerBuilder.queryApiPort(this.queryAPIPort != null ? this.queryAPIPort : 0);
        comServerBuilder.eventRegistrationPort(this.eventRegistrationPort != null ? this.eventRegistrationPort : 0);
        comServerBuilder.storeTaskQueueSize(this.storeTaskQueueSize != null ? this.storeTaskQueueSize : 0);
        comServerBuilder.storeTaskThreadPriority(this.storeTaskThreadPriority != null ? this.storeTaskThreadPriority : 0);
        comServerBuilder.numberOfStoreTaskThreads(this.numberOfStoreTaskThreads != null ? this.numberOfStoreTaskThreads : 0);
        return comServerBuilder;
    }

    public OnlineComServer updateTo(OnlineComServer onlineComServer, EngineConfigurationService engineConfigurationService) {
        onlineComServer.setServerName(this.serverName);
        onlineComServer.setQueryApiPort(this.queryAPIPort != null ? this.queryAPIPort : 0);
        onlineComServer.setEventRegistrationPort(this.eventRegistrationPort != null ? this.eventRegistrationPort : 0);
        onlineComServer.setStatusPort(this.statusPort != null ? this.statusPort : 0);
        onlineComServer.setStoreTaskQueueSize(this.storeTaskQueueSize != null ? this.storeTaskQueueSize : 0);
        onlineComServer.setStoreTaskThreadPriority(this.storeTaskThreadPriority != null ? this.storeTaskThreadPriority : 0);
        onlineComServer.setNumberOfStoreTaskThreads(this.numberOfStoreTaskThreads != null ? this.numberOfStoreTaskThreads : 0);
        super.updateTo(onlineComServer, engineConfigurationService);
        return onlineComServer;
    }

    @Override
    protected OnlineComServer.OnlineComServerBuilder createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newOnlineComServerBuilder();
    }
}
