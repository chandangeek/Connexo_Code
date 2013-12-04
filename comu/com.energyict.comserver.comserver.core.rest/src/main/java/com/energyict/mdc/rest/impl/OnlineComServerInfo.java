package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="comServerType")
public class OnlineComServerInfo extends InboundOutboundComServerInfo<OnlineComServerShadow> {

    public OnlineComServerInfo() {
    }

    /**
     * Create Info based on comserver properties and comports
     */
    public OnlineComServerInfo(final OnlineComServer onlineComServer, List<ComPort> comPorts) {
        super(onlineComServer, comPorts);
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

    public OnlineComServerShadow writeToShadow(OnlineComServerShadow comServerShadow) {
        super.writeToShadow(comServerShadow);
        comServerShadow.setQueryAPIPostUri(queryAPIPostUri);
        comServerShadow.setUsesDefaultQueryAPIPostUri(usesDefaultQueryAPIPostUri);
        comServerShadow.setEventRegistrationUri(eventRegistrationUri);
        comServerShadow.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri);
        comServerShadow.setStoreTaskQueueSize(storeTaskQueueSize);
        comServerShadow.setStoreTaskThreadPriority(storeTaskThreadPriority);
        comServerShadow.setNumberOfStoreTaskThreads(numberOfStoreTaskThreads);

        updateInboundComPorts(comServerShadow);
        updateOutboundComPorts(comServerShadow);

        return comServerShadow;
    }

    public OnlineComServerShadow asShadow() {
        OnlineComServerShadow shadow = new OnlineComServerShadow();
        this.writeToShadow(shadow);
        return shadow;
    }

}
