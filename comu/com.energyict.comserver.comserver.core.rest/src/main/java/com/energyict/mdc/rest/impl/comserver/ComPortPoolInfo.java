package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InboundComPortPoolInfo.class, name = "inbound"),
        @JsonSubTypes.Type(value = OutboundComPortPoolInfo.class, name = "outbound")})
public abstract class ComPortPoolInfo<S extends ComPortPool> {
    public long id;
    public String name;
    public boolean active;
    public String description;
    public boolean obsoleteFlag;
    public Date obsoleteDate;
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
    public ComPortType type;
    public List<InboundComPortInfo> inboundComPortInfos;
    public long discoveryProtocolPluggableClassId;
    public List<OutboundComPortInfo> outboundComPortInfos;
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

    protected S writeTo(S source,EngineModelService engineModelService) {
        source.setName(this.name);
        source.setDescription(this.description);
        source.setComPortType(this.type);
        source.setActive(this.active);
        return source;
    }

    protected abstract S createNew(EngineModelService engineModelService);
}
