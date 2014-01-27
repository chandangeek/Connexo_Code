package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
@JsonSubTypes({
     @JsonSubTypes.Type(value = OnlineComServerInfo.class, name = "Online"),
     @JsonSubTypes.Type(value = OfflineComServerInfo.class, name = "Offline"),
     @JsonSubTypes.Type(value = RemoteComServerInfo.class, name = "Remote") })
public abstract class ComServerInfo<S extends ComServer> {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("active")
    public boolean active;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel serverLogLevel;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel communicationLogLevel;
    @JsonProperty("changesInterPollDelay")
    public TimeDurationInfo changesInterPollDelay;
    @JsonProperty("schedulingInterPollDelay")
    public TimeDurationInfo schedulingInterPollDelay;
    @JsonProperty("inboundComPorts")
    public List<InboundComPortInfo> inboundComPorts;
    @JsonProperty("outboundComPorts")
    public List<OutboundComPortInfo> outboundComPorts;
    @JsonProperty("onlineComServerId")
    public Long onlineComServerId;
    @JsonProperty("queryAPIUsername")
    public String queryAPIUsername;
    @JsonProperty("queryAPIPassword")
    public String queryAPIPassword;
    @JsonProperty("queryAPIPostUri")
    public String queryAPIPostUri;
    @JsonProperty("usesDefaultQueryAPIPostUri")
    public boolean usesDefaultQueryAPIPostUri;
    @JsonProperty("eventRegistrationUri")
    public String eventRegistrationUri;
    @JsonProperty("usesDefaultEventRegistrationUri")
    public boolean usesDefaultEventRegistrationUri;
    @JsonProperty("storeTaskQueueSize")
    public int storeTaskQueueSize;
    @JsonProperty("numberOfStoreTaskThreads")
    public int numberOfStoreTaskThreads;
    @JsonProperty("storeTaskThreadPriority")
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

    protected abstract S createNew(EngineModelService engineModelService);
}
