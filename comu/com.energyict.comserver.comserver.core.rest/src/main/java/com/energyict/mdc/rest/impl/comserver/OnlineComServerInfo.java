package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="comServerType")
public class OnlineComServerInfo extends ComServerInfo<OnlineComServer> {

    public OnlineComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public OnlineComServerInfo(final OnlineComServer onlineComServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService) {
        super(onlineComServer, comPorts, engineConfigurationService);
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
        this.usesDefaultQueryAPIPostUri = onlineComServer.usesDefaultQueryApiPostUri();
        this.eventRegistrationUri = onlineComServer.getEventRegistrationUri();
        this.usesDefaultEventRegistrationUri = onlineComServer.usesDefaultEventRegistrationUri();
        this.storeTaskQueueSize = onlineComServer.getStoreTaskQueueSize();
        this.numberOfStoreTaskThreads = onlineComServer.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = onlineComServer.getStoreTaskThreadPriority();
    }

    public OnlineComServer writeTo(OnlineComServer comServerSource,EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerSource, engineConfigurationService);
        Optional<String> queryAPIPostUri = Optional.ofNullable(this.queryAPIPostUri);
        if(queryAPIPostUri.isPresent()) {
            comServerSource.setQueryAPIPostUri(queryAPIPostUri.get());
        }
        Optional<Boolean> usesDefaultQueryAPIPostUri = Optional.ofNullable(this.usesDefaultQueryAPIPostUri);
        if(usesDefaultQueryAPIPostUri.isPresent()) {
            comServerSource.setUsesDefaultQueryAPIPostUri(usesDefaultQueryAPIPostUri.get());
        }
        Optional<String> eventRegistrationUri = Optional.ofNullable(this.eventRegistrationUri);
        if(eventRegistrationUri.isPresent()) {
            comServerSource.setEventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<Boolean> usesDefaultEventRegistrationUri = Optional.ofNullable(this.usesDefaultEventRegistrationUri);
        if(usesDefaultEventRegistrationUri.isPresent()) {
            comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri.get());
        }
        Optional<Integer> storeTaskQueueSize = Optional.ofNullable(this.storeTaskQueueSize);
        if(storeTaskQueueSize.isPresent()) {
            comServerSource.setStoreTaskQueueSize(storeTaskQueueSize.get());
        }
        Optional<Integer> storeTaskThreadPriority = Optional.ofNullable(this.storeTaskThreadPriority);
        if(storeTaskThreadPriority.isPresent()) {
            comServerSource.setStoreTaskThreadPriority(storeTaskThreadPriority.get());
        }
        Optional<Integer> numberOfStoreTaskThreads = Optional.ofNullable(this.numberOfStoreTaskThreads);
        if(numberOfStoreTaskThreads.isPresent()) {
            comServerSource.setNumberOfStoreTaskThreads(numberOfStoreTaskThreads.get());
        }

        return comServerSource;
    }

    @Override
    protected OnlineComServer createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newOnlineComServerInstance();
    }
}
