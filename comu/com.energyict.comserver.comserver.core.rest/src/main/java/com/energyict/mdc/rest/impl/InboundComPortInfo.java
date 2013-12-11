package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.shadow.ports.InboundComPortShadow;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comPortType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpInboundComPortInfo.class, name = "TCP"),
        @JsonSubTypes.Type(value = UdpInboundComPortInfo.class, name = "UDP"),
        @JsonSubTypes.Type(value = ServletInboundComPortInfo.class, name = "SERVLET"),
        @JsonSubTypes.Type(value = ModemInboundComPortInfo.class, name = "SERIAL") })
public abstract class InboundComPortInfo<T extends InboundComPortShadow> extends ComPortInfo<T> {

    protected InboundComPortInfo() {
        this.direction = "inbound";
    }

    public InboundComPortInfo(InboundComPort comPort) {
        super(comPort);
        this.direction = "inbound";
        this.comPortPool_id = comPort.getComPortPool()!=null?comPort.getComPortPool().getId():0;
    }

    @Override
    protected void writeToShadow(T shadow) {
        super.writeToShadow(shadow);
        shadow.setInboundComPortPoolId(this.comPortPool_id);
    }
}
