package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonTypeInfo;

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
    public OnlineComServerInfo(final OnlineComServer onlineComServer, List<ComPort> comPorts, EngineModelService engineModelService) {
        super(onlineComServer, comPorts, engineModelService);
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

    public OnlineComServer writeTo(OnlineComServer comServerSource,EngineModelService engineModelService) {
        super.writeTo(comServerSource,engineModelService);
        Optional<String> queryAPIPostUri = Optional.fromNullable(this.queryAPIPostUri);
        if(queryAPIPostUri.isPresent()) {
            comServerSource.setQueryAPIPostUri(queryAPIPostUri.get());
        }
        Optional<Boolean> usesDefaultQueryAPIPostUri = Optional.fromNullable(this.usesDefaultQueryAPIPostUri);
        if(usesDefaultQueryAPIPostUri.isPresent()) {
            comServerSource.setUsesDefaultQueryAPIPostUri(usesDefaultQueryAPIPostUri.get());
        }
        Optional<String> eventRegistrationUri = Optional.fromNullable(this.eventRegistrationUri);
        if(eventRegistrationUri.isPresent()) {
            comServerSource.setEventRegistrationUri(eventRegistrationUri.get());
        }
        Optional<Boolean> usesDefaultEventRegistrationUri = Optional.fromNullable(this.usesDefaultEventRegistrationUri);
        if(usesDefaultEventRegistrationUri.isPresent()) {
            comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri.get());
        }
        Optional<Integer> storeTaskQueueSize = Optional.fromNullable(this.storeTaskQueueSize);
        if(storeTaskQueueSize.isPresent()) {
            comServerSource.setStoreTaskQueueSize(storeTaskQueueSize.get());
        }
        Optional<Integer> storeTaskThreadPriority = Optional.fromNullable(this.storeTaskThreadPriority);
        if(storeTaskThreadPriority.isPresent()) {
            comServerSource.setStoreTaskThreadPriority(storeTaskThreadPriority.get());
        }
        Optional<Integer> numberOfStoreTaskThreads = Optional.fromNullable(this.numberOfStoreTaskThreads);
        if(numberOfStoreTaskThreads.isPresent()) {
            comServerSource.setNumberOfStoreTaskThreads(numberOfStoreTaskThreads.get());
        }

        return comServerSource;
    }

    @Override
    protected OnlineComServer createNew(EngineModelService engineModelService) {
        return engineModelService.newOnlineComServerInstance();
    }
}
