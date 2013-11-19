package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comPortDescriptor")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpInboundComPortInfo.class, name = "TCP_INBOUND"),
        @JsonSubTypes.Type(value = ModemInboundComPortInfo.class, name = "MODEM_INBOUND"),
        @JsonSubTypes.Type(value = OutboundComPortInfo.class, name = "OUTBOUND"),
        @JsonSubTypes.Type(value = UdpInboundComPortInfo.class, name = "UDP_INBOUND") })
public abstract class ComPortInfo<T extends ComPortShadow> {

    public int id;
    public String name;
    public String description;
    public boolean active;
    public boolean bound;
    public ComPortType comPortType;
    public int comserver_id;
    public int numberOfSimultaneousConnections;
    public Date modificationDate;

    public ComPortInfo() {
    }

    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.description = comPort.getDescription();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comserver_id = comPort.getComServer().getId();
        this.comPortType = comPort.getComPortType();
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.modificationDate = comPort.getModificationDate();
    }

    protected void writeToShadow(T shadow) {
        shadow.setName(name);
        shadow.setDescription(description);
        shadow.setComServerId(comserver_id);
        shadow.setActive(active);
        shadow.setType(this.comPortType);
    }

    public abstract T asShadow();
}
