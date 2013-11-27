package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPortPool;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.shadow.ports.ComPortPoolShadow;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InboundComPortPoolInfo.class, name = "inbound"),
        @JsonSubTypes.Type(value = OutboundComPortPoolInfo.class, name = "outbound")})
public abstract class ComPortPoolInfo<T extends ComPortPoolShadow> {
    public int id;
    public String name;
    public boolean active;
    public String description;
    public boolean obsoleteFlag;
    public Date obsoleteDate;
    public ComPortType type;
    public List<InboundComPortInfo> inboundComPorts;
    public int discoveryProtocolPluggableClassId;
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

    protected void writeToShadow(T shadow) {
        shadow.setName(this.name);
        shadow.setDescription(this.description);
        shadow.setType(this.type);
        shadow.setActive(this.active);
        shadow.setId(this.id);
    }

    abstract public T asShadow();

}
