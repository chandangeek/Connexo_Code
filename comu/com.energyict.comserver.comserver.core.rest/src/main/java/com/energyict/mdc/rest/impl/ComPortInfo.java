package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comPortType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpComPortInfo.class, name = "TCP"),
        @JsonSubTypes.Type(value = ModemComPortInfo.class, name = "Serial"),
        @JsonSubTypes.Type(value = UdpComPortInfo.class, name = "UDP") })
public abstract class ComPortInfo {
    public int id;
    public String name;
    public String description;
    public boolean active;
    public boolean bound;
    public int comserver_id;

    public ComPortInfo() {
    }

    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.description = comPort.getDescription();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comserver_id = comPort.getComServer().getId();
    }

    protected void writeToShadow(ComPortShadow shadow) {
        shadow.setName(name);
        shadow.setDescription(description);
        shadow.setComServerId(comserver_id);
        shadow.setActive(active);
    }

    public abstract ComPortShadow asShadow();
}
