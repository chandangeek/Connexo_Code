package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.servers.ComServerShadow;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
@JsonSubTypes({
     @JsonSubTypes.Type(value = OnlineComServerInfo.class, name = "Online"),
     @JsonSubTypes.Type(value = OfflineComServerInfo.class, name = "Offline"),
     @JsonSubTypes.Type(value = RemoteComServerInfo.class, name = "Remote") })
public abstract class ComServerInfo<S extends ComServerShadow> {

    public int id;
//    public String comServerType;
    public String name;
    public boolean active;
    public ComServer.LogLevel serverLogLevel;
    public ComServer.LogLevel communicationLogLevel;
    public TimeDurationInfo changesInterPollDelay;
    public TimeDurationInfo schedulingInterPollDelay;
    public List<InboundComPortInfo<? extends ComPortShadow>> inboundComPorts;
    public List<OutboundComPortInfo> outboundComPorts;
    public Integer onlineComServerId;
    public String queryAPIUsername;
    public String queryAPIPassword;
    public String queryAPIPostUri;
    public Boolean usesDefaultQueryAPIPostUri;
    public String eventRegistrationUri;
    public Boolean usesDefaultEventRegistrationUri;
    public Integer storeTaskQueueSize;
    public Integer numberOfStoreTaskThreads;
    public Integer storeTaskThreadPriority;

    public ComServerInfo() {
    }

    /**
     * Creates info object containing only ComServer properties, not comports
     */
    public ComServerInfo(ComServer comServer) {
        this.id=comServer.getId();
        this.name = comServer.getName();
        this.active = comServer.isActive();
        this.serverLogLevel = comServer.getServerLogLevel();
        this.communicationLogLevel = comServer.getCommunicationLogLevel();
        this.changesInterPollDelay = comServer.getChangesInterPollDelay()!=null?new TimeDurationInfo(comServer.getChangesInterPollDelay()):null;
        this.schedulingInterPollDelay = comServer.getSchedulingInterPollDelay()!=null?new TimeDurationInfo(comServer.getSchedulingInterPollDelay()):null;
    }

    /**
     * Creates info object containing both ComServer properties and comports
     */
    public ComServerInfo(ComServer comServer, List<ComPort> comPorts) {
        this(comServer);
        inboundComPorts = new ArrayList<>();
        outboundComPorts = new ArrayList<>();
        for (final ComPort comPort : comPorts) {
            if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                inboundComPorts.add(ComPortInfoFactory.asInboundInfo(comPort));
            } else {
                outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(comPort));
            }
        }
    }

    public S writeToShadow(S shadow) {
        shadow.setName(name);
        shadow.setActive(active);
        shadow.setServerLogLevel(serverLogLevel);
        shadow.setCommunicationLogLevel(communicationLogLevel);
        if (changesInterPollDelay!=null) {
            shadow.setChangesInterPollDelay(changesInterPollDelay.asTimeDuration());
        }
        if (schedulingInterPollDelay!=null) {
            shadow.setSchedulingInterPollDelay(schedulingInterPollDelay.asTimeDuration());
        }

        return shadow;
    }

    abstract public S asShadow();

}
