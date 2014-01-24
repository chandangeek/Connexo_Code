package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UdpInboundComPortInfo extends InboundComPortInfo<UDPBasedInboundComPort, UDPBasedInboundComPort.UDPBasedInboundComPortBuilder> {

    public UdpInboundComPortInfo() {
        this.comPortType = ComPortType.UDP;
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
        if (comPort.getComPortPool()!=null) {
            this.comPortPool_id = comPort.getComPortPool().getId();
        }
        this.portNumber = comPort.getPortNumber();
        this.bufferSize = comPort.getBufferSize();
    }

    @Override
    protected void writeTo(UDPBasedInboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        InboundComPortPool inboundComPortPool = engineModelService.findInboundComPortPool(this.comPortPool_id);
        if(inboundComPortPool!=null){
            source.setComPortPool(inboundComPortPool);
        } else {
            throw new WebApplicationException("Failed to update ComPort", Response.Status.INTERNAL_SERVER_ERROR);
        }
        source.setPortNumber(this.portNumber);
        source.setBufferSize(this.bufferSize);
    }

    @Override
    protected UDPBasedInboundComPort.UDPBasedInboundComPortBuilder build(UDPBasedInboundComPort.UDPBasedInboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(builder.
                portNumber(portNumber).
                bufferSize(bufferSize).
                numberOfSimultaneousConnections(numberOfSimultaneousConnections), engineModelService);
    }

    @Override
    protected UDPBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newUDPBasedInboundComPort(), engineModelService).add();
    }
}
