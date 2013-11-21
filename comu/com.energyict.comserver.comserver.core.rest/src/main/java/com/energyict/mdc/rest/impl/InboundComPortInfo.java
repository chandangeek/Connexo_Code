package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comPortType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpInboundComPortInfo.class, name = "TCP"),
        @JsonSubTypes.Type(value = ModemInboundComPortInfo.class, name = "MODEM"),
        @JsonSubTypes.Type(value = UdpInboundComPortInfo.class, name = "UDP") })
public abstract class InboundComPortInfo<T extends ComPortShadow> extends ComPortInfo<T> {

    protected InboundComPortInfo() {
    }

    public InboundComPortInfo(InboundComPort comPort) {
        super(comPort);
    }
}
