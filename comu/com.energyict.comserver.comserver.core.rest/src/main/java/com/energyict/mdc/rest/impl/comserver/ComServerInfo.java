package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Optional;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
@JsonSubTypes({
     @JsonSubTypes.Type(value = OnlineComServerInfo.class, name = "Online"),
     @JsonSubTypes.Type(value = OfflineComServerInfo.class, name = "Offline"),
     @JsonSubTypes.Type(value = RemoteComServerInfo.class, name = "Remote") })
public abstract class ComServerInfo<S extends ComServer> {

    public long id;
    public String name;
    public Boolean active;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel serverLogLevel;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel communicationLogLevel;
    public TimeDurationInfo changesInterPollDelay;
    public TimeDurationInfo schedulingInterPollDelay;
    public List<InboundComPortInfo> inboundComPorts;
    public List<OutboundComPortInfo> outboundComPorts;
    public Long onlineComServerId;
    public String queryAPIPostUri;
    public Boolean usesDefaultQueryAPIPostUri;
    public String eventRegistrationUri;
    public Boolean usesDefaultEventRegistrationUri;
    public String statusUri;
    public Boolean usesDefaultStatusUri;
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
        this.changesInterPollDelay = TimeDurationInfo.of(comServer.getChangesInterPollDelay());
        this.schedulingInterPollDelay = TimeDurationInfo.of(comServer.getSchedulingInterPollDelay());
    }

    /**
     * Creates info object containing both ComServer properties and comports
     */
    public ComServerInfo(ComServer comServer, List<ComPort> comPorts, EngineModelService engineModelService) {
        this(comServer);
        inboundComPorts = new ArrayList<>();
        outboundComPorts = new ArrayList<>();
        for (final ComPort comPort : comPorts) {
            if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                inboundComPorts.add(ComPortInfoFactory.asInboundInfo(comPort));
            } else {
                outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(comPort, engineModelService));
            }
        }
    }

    public S writeTo(S source,EngineModelService engineModelService) {
        Optional<String> name = Optional.ofNullable(this.name);
        if(name.isPresent()) {
            source.setName(name.get());
        }
        Optional<Boolean> active = Optional.ofNullable(this.active);
        if(active.isPresent()) {
            source.setActive(active.get());
        }
        Optional<ComServer.LogLevel> serverLogLevel = Optional.ofNullable(this.serverLogLevel);
        if(serverLogLevel.isPresent()) {
            source.setServerLogLevel(serverLogLevel.get());
        }
        Optional<ComServer.LogLevel> communicationLogLevel = Optional.ofNullable(this.communicationLogLevel);
        if(communicationLogLevel.isPresent()) {
            source.setCommunicationLogLevel(communicationLogLevel.get());
        }
        Optional<TimeDurationInfo> changesInterPollDelay = Optional.ofNullable(this.changesInterPollDelay);
        if (changesInterPollDelay.isPresent()) {
            source.setChangesInterPollDelay(changesInterPollDelay.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> schedulingInterPollDelay = Optional.ofNullable(this.schedulingInterPollDelay);
        if (schedulingInterPollDelay.isPresent()) {
            source.setSchedulingInterPollDelay(schedulingInterPollDelay.get().asTimeDuration());
        }

        return source;
    }

    protected abstract S createNew(EngineModelService engineModelService);

}