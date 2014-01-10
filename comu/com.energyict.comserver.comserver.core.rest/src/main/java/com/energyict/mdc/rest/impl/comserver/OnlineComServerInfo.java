package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.impl.ServerTCPBasedInboundComPort;
import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="comServerType")
public class OnlineComServerInfo extends InboundOutboundComServerInfo<OnlineComServer> {

    private EngineModelService engineModelService;

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

    public OnlineComServer writeTo(OnlineComServer comServerSource) {
        super.writeTo(comServerSource);
        comServerSource.setQueryAPIPostUri(queryAPIPostUri);
        comServerSource.setUsesDefaultQueryAPIPostUri(usesDefaultQueryAPIPostUri);
        comServerSource.setEventRegistrationUri(eventRegistrationUri);
        comServerSource.setUsesDefaultEventRegistrationUri(usesDefaultEventRegistrationUri);
        comServerSource.setStoreTaskQueueSize(storeTaskQueueSize);
        comServerSource.setStoreTaskThreadPriority(storeTaskThreadPriority);
        comServerSource.setNumberOfStoreTaskThreads(numberOfStoreTaskThreads);

        for (InboundComPortInfo<? extends ComPort> inboundComPort : this.inboundComPorts) {
            TCPBasedInboundComPort newPort = (TCPBasedInboundComPort) engineModelService.newTCPBasedInbound(comServerSource);
            inboundComPort.writeTo(newPort);
        }

        updateInboundComPorts(comServerSource);
        updateOutboundComPorts(comServerSource);

        return comServerSource;
    }

    public OnlineComServerShadow asShadow() {
        OnlineComServerShadow shadow = new OnlineComServerShadow();
        this.writeTo(shadow);
        return shadow;
    }

}
