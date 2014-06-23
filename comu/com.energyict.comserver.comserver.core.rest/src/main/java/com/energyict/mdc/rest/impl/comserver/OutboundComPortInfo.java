package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comPortType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TcpOutboundComPortInfo.class, name = "TCP"),
        @JsonSubTypes.Type(value = UdpOutboundComPortInfo.class, name = "UDP"),
        @JsonSubTypes.Type(value = ModemOutboundComPortInfo.class, name = "SERIAL") })
public abstract class OutboundComPortInfo extends ComPortInfo<OutboundComPort, OutboundComPort.OutboundComPortBuilder> {

    public OutboundComPortInfo() {
        this.direction = "outbound";
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
        this.direction = "outbound";
    }

    @Override
    protected void writeTo(OutboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source, engineModelService);
        source.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
    }

    @Override
    protected OutboundComPort.OutboundComPortBuilder build(OutboundComPort.OutboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(builder.comPortType(comPortType), engineModelService);
    }

    @Override
    protected OutboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newOutboundComPort(this.name, this.numberOfSimultaneousConnections), engineModelService).add();
    }

}
