package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.InboundComPort;
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
public abstract class InboundComPortInfo<T extends InboundComPort> extends ComPortInfo<T> {

    protected InboundComPortInfo() {
        this.direction = "inbound";
    }

    public InboundComPortInfo(InboundComPort comPort) {
        super(comPort);
        this.direction = "inbound";
        this.comPortPool_id = comPort.getComPortPool()!=null?comPort.getComPortPool().getId():0L;
    }

    @Override
    protected void writeTo(T source) {
        super.writeTo(source);
        source.setComPortPool(this.comPortPool_id);
    }
}
