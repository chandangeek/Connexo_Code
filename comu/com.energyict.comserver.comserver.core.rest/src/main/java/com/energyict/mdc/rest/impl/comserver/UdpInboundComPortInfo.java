package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UdpInboundComPortInfo extends InboundComPortInfo<UDPBasedInboundComPort, UDPBasedInboundComPort.UDPBasedInboundComPortBuilder> {

    public UdpInboundComPortInfo() {
        this.comPortType = ComPortType.UDP;
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
        this.portNumber = comPort.getPortNumber();
        this.bufferSize = comPort.getBufferSize();
    }

    @Override
    protected void writeTo(UDPBasedInboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        Optional<Integer> portNumber = Optional.fromNullable(this.portNumber);
        if(portNumber.isPresent()) {
            source.setPortNumber(portNumber.get());
        }
        Optional<Integer> bufferSize = Optional.fromNullable(this.bufferSize);
        if(bufferSize.isPresent()) {
            source.setBufferSize(bufferSize.get());
        }
    }

    @Override
    protected UDPBasedInboundComPort.UDPBasedInboundComPortBuilder build(UDPBasedInboundComPort.UDPBasedInboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(builder.
                bufferSize(bufferSize)
                , engineModelService);
    }

    @Override
    protected UDPBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newUDPBasedInboundComPort(this.name, this.numberOfSimultaneousConnections, this.portNumber), engineModelService).add();
    }
}
