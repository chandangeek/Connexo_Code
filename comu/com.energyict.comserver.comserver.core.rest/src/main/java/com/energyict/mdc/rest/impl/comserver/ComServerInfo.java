/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
@JsonSubTypes({
     @JsonSubTypes.Type(value = OnlineComServerInfo.class, name = "Online"),
     @JsonSubTypes.Type(value = OfflineComServerInfo.class, name = "Offline"),
     @JsonSubTypes.Type(value = RemoteComServerInfo.class, name = "Remote") })
public abstract class ComServerInfo<B extends ComServer.ComServerBuilder,C extends ComServer> {

    public long id;
    public String name;
    public Boolean active;
    public String displayComServerType;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel serverLogLevel;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel communicationLogLevel;
    public TimeDurationInfo changesInterPollDelay;
    public TimeDurationInfo schedulingInterPollDelay;
    public List<InboundComPortInfo> inboundComPorts;
    public List<OutboundComPortInfo> outboundComPorts;
    public Long onlineComServerId;
    public String serverName;
    public Integer queryAPIPort;
    public Integer eventRegistrationPort;
    public Integer statusPort;
    public Integer storeTaskQueueSize;
    public Integer numberOfStoreTaskThreads;
    public Integer storeTaskThreadPriority;
    public long version;

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
        this.version = comServer.getVersion();
    }

    /**
     * Creates info object containing both ComServer properties and comports
     */
    public ComServerInfo(ComServer comServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService, ComPortInfoFactory comPortInfoFactory) {
        this(comServer);
        inboundComPorts = new ArrayList<>();
        outboundComPorts = new ArrayList<>();
        for (final ComPort comPort : comPorts) {
            if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                inboundComPorts.add(comPortInfoFactory.asInboundInfo(comPort));
            } else {
                outboundComPorts.add(comPortInfoFactory.asOutboundInfo(comPort, engineConfigurationService));
            }
        }
    }

    public B writeTo(B comServerBuilder, EngineConfigurationService engineConfigurationService) {
        Optional<String> name = Optional.ofNullable(this.name);
        if(name.isPresent()) {
            comServerBuilder.name(name.get());
        }
        Optional<Boolean> active = Optional.ofNullable(this.active);
        if(active.isPresent()) {
            comServerBuilder.active(active.get());
        }
        Optional<ComServer.LogLevel> serverLogLevel = Optional.ofNullable(this.serverLogLevel);
        if(serverLogLevel.isPresent()) {
            comServerBuilder.serverLogLevel(serverLogLevel.get());
        }
        Optional<ComServer.LogLevel> communicationLogLevel = Optional.ofNullable(this.communicationLogLevel);
        if(communicationLogLevel.isPresent()) {
            comServerBuilder.communicationLogLevel(communicationLogLevel.get());
        }
        Optional<TimeDurationInfo> changesInterPollDelay = Optional.ofNullable(this.changesInterPollDelay);
        if (changesInterPollDelay.isPresent()) {
            comServerBuilder.changesInterPollDelay(changesInterPollDelay.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> schedulingInterPollDelay = Optional.ofNullable(this.schedulingInterPollDelay);
        if (schedulingInterPollDelay.isPresent()) {
            comServerBuilder.schedulingInterPollDelay(schedulingInterPollDelay.get().asTimeDuration());
        }

        return comServerBuilder;
    }

    public C updateTo(C comServer, EngineConfigurationService engineConfigurationService) {
        Optional<String> name = Optional.ofNullable(this.name);
        if(name.isPresent()) {
            comServer.setName(name.get());
        }
        Optional<Boolean> active = Optional.ofNullable(this.active);
        if(active.isPresent()) {
            comServer.setActive(active.get());
        }
        Optional<ComServer.LogLevel> serverLogLevel = Optional.ofNullable(this.serverLogLevel);
        if(serverLogLevel.isPresent()) {
            comServer.setServerLogLevel(serverLogLevel.get());
        }
        Optional<ComServer.LogLevel> communicationLogLevel = Optional.ofNullable(this.communicationLogLevel);
        if(communicationLogLevel.isPresent()) {
            comServer.setCommunicationLogLevel(communicationLogLevel.get());
        }
        Optional<TimeDurationInfo> changesInterPollDelay = Optional.ofNullable(this.changesInterPollDelay);
        if (changesInterPollDelay.isPresent()) {
            comServer.setChangesInterPollDelay(changesInterPollDelay.get().asTimeDuration());
        }
        Optional<TimeDurationInfo> schedulingInterPollDelay = Optional.ofNullable(this.schedulingInterPollDelay);
        if (schedulingInterPollDelay.isPresent()) {
            comServer.setSchedulingInterPollDelay(schedulingInterPollDelay.get().asTimeDuration());
        }
        comServer.update();
        return comServer;
    }

    protected abstract B createNew(EngineConfigurationService engineConfigurationService);

}