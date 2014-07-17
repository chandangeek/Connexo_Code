package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

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
    public Date obsoleteDate;
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
    public ComPortType type;
    public List<InboundComPortInfo> inboundComPorts;
    public Long discoveryProtocolPluggableClassId;
    public List<OutboundComPortInfo> outboundComPorts;
    public TimeDurationInfo taskExecutionTimeout;

    public ComPortPoolInfo() {
    }

    public ComPortPoolInfo(ComPortPool comPortPool) {
        this.id = comPortPool.getId();
        this.name = comPortPool.getName();
        this.active = comPortPool.isActive();
        this.description = comPortPool.getDescription();
        this.obsoleteDate = comPortPool.getObsoleteDate();
        this.obsoleteFlag = comPortPool.isObsolete();
        this.type = comPortPool.getComPortType();
    }

    protected S writeTo(S source, ProtocolPluggableService protocolPluggableService) {
        Optional<String> name = Optional.fromNullable(this.name);
        if(name.isPresent()) {
            source.setName(name.get());
        }
        Optional<String> description = Optional.fromNullable(this.description);
        if(description.isPresent()) {
            source.setDescription(description.get());
        }
        Optional<ComPortType> type = Optional.fromNullable(this.type);
        if(type.isPresent()) {
            source.setComPortType(type.get());
        }
        Optional<Boolean> active = Optional.fromNullable(this.active);
        if(active.isPresent()) {
            source.setActive(active.get());
        }

        return source;
    }

    protected abstract S createNew(EngineModelService engineModelService);

    protected abstract void handlePools(S comPortPool, EngineModelService engineModelService, boolean all);
}
