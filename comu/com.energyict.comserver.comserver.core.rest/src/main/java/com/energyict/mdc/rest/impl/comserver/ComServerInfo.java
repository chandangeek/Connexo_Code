package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
@JsonSubTypes({
     @JsonSubTypes.Type(value = OnlineComServerInfo.class, name = "Online"),
     @JsonSubTypes.Type(value = OfflineComServerInfo.class, name = "Offline"),
     @JsonSubTypes.Type(value = RemoteComServerInfo.class, name = "Remote") })
public abstract class ComServerInfo<S extends ComServer> {

    public long id;
    public String name;
    public boolean active;
    public ComServer.LogLevel serverLogLevel;
    public ComServer.LogLevel communicationLogLevel;
    public TimeDurationInfo changesInterPollDelay;
    public TimeDurationInfo schedulingInterPollDelay;
    public List<InboundComPortInfo<? extends InboundComPort>> inboundComPortInfos;
    public List<OutboundComPortInfo> outboundComPortInfos;
    public Long onlineComServerId;
    public String queryAPIUsername;
    public String queryAPIPassword;
    public String queryAPIPostUri;
    public boolean usesDefaultQueryAPIPostUri;
    public String eventRegistrationUri;
    public boolean usesDefaultEventRegistrationUri;
    public int storeTaskQueueSize;
    public int numberOfStoreTaskThreads;
    public int storeTaskThreadPriority;

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
        inboundComPortInfos = new ArrayList<>();
        outboundComPortInfos = new ArrayList<>();
        for (final ComPort comPort : comPorts) {
            if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                inboundComPortInfos.add(ComPortInfoFactory.asInboundInfo(comPort));
            } else {
                outboundComPortInfos.add(ComPortInfoFactory.asOutboundInfo(comPort));
            }
        }
    }

    public S writeTo(S source,EngineModelService engineModelService) {
        source.setName(name);
        source.setActive(active);
        source.setServerLogLevel(serverLogLevel);
        source.setCommunicationLogLevel(communicationLogLevel);
        if (changesInterPollDelay!=null) {
            source.setChangesInterPollDelay(changesInterPollDelay.asTimeDuration());
        }
        if (schedulingInterPollDelay!=null) {
            source.setSchedulingInterPollDelay(schedulingInterPollDelay.asTimeDuration());
        }

        return source;
    }
}
