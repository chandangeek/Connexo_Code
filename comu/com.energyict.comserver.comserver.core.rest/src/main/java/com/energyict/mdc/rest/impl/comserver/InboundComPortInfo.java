package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public abstract class InboundComPortInfo<T extends InboundComPort, B extends InboundComPort.InboundComPortBuilder<B, T>> extends ComPortInfo<T, B> {

    protected InboundComPortInfo() {
        this.direction = "inbound";
    }

    public InboundComPortInfo(InboundComPort comPort) {
        super(comPort);
        this.direction = "inbound";
        this.comPortPool_id = comPort.getComPortPool()!=null?comPort.getComPortPool().getId():0L;
    }

    @Override
    protected void writeTo(T source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        InboundComPortPool inboundComPortPool = null;
        if (this.comPortPool_id!=null) {
            inboundComPortPool=engineModelService.findInboundComPortPool(this.comPortPool_id);
        }
        if(inboundComPortPool!=null){
            source.setComPortPool(inboundComPortPool);
        } else {
            throw new WebApplicationException("Failed to set ComPortPool "+this.comPortPool_id+" on ComPort "+this.id,Response.Status.BAD_REQUEST);
        }
        source.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
    }

    @Override
    protected B build(B builder, EngineModelService engineModelService) {
        super.build(builder, engineModelService);
        InboundComPortPool inboundComPortPool = null;
        if (this.comPortPool_id!=null) {
            inboundComPortPool=engineModelService.findInboundComPortPool(this.comPortPool_id);
        }
        if(inboundComPortPool!=null){
            builder.comPortPool(inboundComPortPool);
        } else {
            throw new WebApplicationException("Failed to set ComPortPool "+this.comPortPool_id+" on ComPort",Response.Status.BAD_REQUEST);
        }
        return builder;
    }

    @Override
    protected abstract ComPort createNew(ComServer comServer, EngineModelService engineModelService);
}
