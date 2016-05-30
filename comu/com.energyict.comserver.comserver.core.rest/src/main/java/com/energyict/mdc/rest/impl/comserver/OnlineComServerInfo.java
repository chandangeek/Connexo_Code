package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Optional;

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
        this.queryAPIPostUri = onlineComServer.getQueryApiPostUri();
        this.eventRegistrationUri = onlineComServer.getEventRegistrationUri();
        this.storeTaskQueueSize = onlineComServer.getStoreTaskQueueSize();
        this.numberOfStoreTaskThreads = onlineComServer.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = onlineComServer.getStoreTaskThreadPriority();
        this.statusUri = onlineComServer.getStatusUri();
    }

    public OnlineComServer.OnlineComServerBuilder writeTo(OnlineComServer.OnlineComServerBuilder comServerBuilder, EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerBuilder, engineConfigurationService);
        Optional<String> queryAPIPostUri = Optional.ofNullable(this.queryAPIPostUri);
        if (queryAPIPostUri.isPresent()) {
            comServerBuilder.queryApiPostUri(queryAPIPostUri.get());
        }
        Optional<String> eventRegistrationUri = Optional.ofNullable(this.eventRegistrationUri);
        if (eventRegistrationUri.isPresent()) {
            comServerBuilder.eventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<String> statusUri = Optional.ofNullable(this.statusUri);
        if (statusUri.isPresent()) {
            comServerBuilder.statusUri(statusUri.get());
        }
        comServerBuilder.storeTaskQueueSize(this.storeTaskQueueSize != null ? this.storeTaskQueueSize : 0);
        comServerBuilder.storeTaskThreadPriority(this.storeTaskThreadPriority != null ? this.storeTaskThreadPriority : 0);
        comServerBuilder.numberOfStoreTaskThreads(this.numberOfStoreTaskThreads != null ? this.numberOfStoreTaskThreads : 0);
        return comServerBuilder;
    }

    public OnlineComServer updateTo(OnlineComServer onlineComServer, EngineConfigurationService engineConfigurationService) {
        Optional<String> queryAPIPostUri = Optional.ofNullable(this.queryAPIPostUri);
        if (queryAPIPostUri.isPresent()) {
            onlineComServer.setQueryAPIPostUri(queryAPIPostUri.get());
        }
        Optional<String> eventRegistrationUri = Optional.ofNullable(this.eventRegistrationUri);
        if (eventRegistrationUri.isPresent()) {
            onlineComServer.setEventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<String> statusUri = Optional.ofNullable(this.statusUri);
        if (statusUri.isPresent()) {
            onlineComServer.setStatusUri(statusUri.get());
        }
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
