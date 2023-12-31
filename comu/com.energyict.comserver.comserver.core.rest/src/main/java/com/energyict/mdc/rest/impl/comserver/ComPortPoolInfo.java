/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InboundComPortPoolInfo.class, name = "Inbound"),
        @JsonSubTypes.Type(value = OutboundComPortPoolInfo.class, name = "Outbound")})
public abstract class ComPortPoolInfo<S extends ComPortPool> {
    public long id;
    public String name;
    public Boolean active;
    public String description;
    public Boolean obsoleteFlag;
    public Instant obsoleteDate;
    public ComPortTypeInfo comPortType;
    public List<InboundComPortInfo> inboundComPorts;
    public Long discoveryProtocolPluggableClassId;
    public List<OutboundComPortInfo> outboundComPorts;
    public TimeDurationInfo taskExecutionTimeout;
    public long version;
    public List<PropertyInfo> properties;
    public long pctHighPrioTasks;
    public long maxPriorityConnections;

    public ComPortPoolInfo() {
    }

    public ComPortPoolInfo(ComPortPool comPortPool) {
        this.id = comPortPool.getId();
        this.name = comPortPool.getName();
        this.active = comPortPool.isActive();
        this.description = comPortPool.getDescription();
        this.obsoleteDate = comPortPool.getObsoleteDate();
        this.obsoleteFlag = comPortPool.isObsolete();
        this.comPortType = new ComPortTypeInfo(comPortPool.getComPortType());
        this.version = comPortPool.getVersion();
        this.pctHighPrioTasks = comPortPool.getPctHighPrioTasks();
    }

    protected S writeTo(S target) {
        target.setName(this.name);
        if (this.notNull(this.description)) {
            target.setDescription(this.description);
        }
        if (this.active != null) {
            target.setActive(this.active);
        }

        target.setPctHighPrioTasks(this.pctHighPrioTasks);
        return target;
    }

    protected boolean notNull(String aString) {
        return aString != null && !aString.isEmpty();
    }

    protected S writeTo(S target, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        return this.writeTo(target);
    }

    protected abstract S createNew(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils);

}
