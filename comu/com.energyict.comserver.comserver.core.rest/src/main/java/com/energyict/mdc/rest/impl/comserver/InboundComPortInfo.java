package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public abstract class InboundComPortInfo<T extends InboundComPort, B extends InboundComPort.InboundComPortBuilder<B, T>> extends ComPortInfo<T, B> {

    public Long comPortPool_id;

    protected InboundComPortInfo() {
        this.direction = "inbound";
    }

    public InboundComPortInfo(InboundComPort comPort) {
        super(comPort);
        this.direction = "inbound";
        Optional<InboundComPortPool> comPortPool = Optional.ofNullable(comPort.getComPortPool());
        if(comPortPool.isPresent()) {
            this.comPortPool_id = comPort.getComPortPool().getId();
        }
    }

    @Override
    protected void writeTo(T source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        Optional<Long> comPortPool_id = Optional.ofNullable(this.comPortPool_id);
        Optional<InboundComPortPool> inboundComPortPool = Optional.empty();
        if (comPortPool_id.isPresent()) {
            inboundComPortPool=Optional.ofNullable(engineModelService.findInboundComPortPool(comPortPool_id.get()));
            if(inboundComPortPool.isPresent()) {
                source.setComPortPool(inboundComPortPool.get());
            }
        }
    }

    @Override
    protected B build(B builder, EngineModelService engineModelService) {
        super.build(builder, engineModelService);
        Optional<Long> comPortPool_id = Optional.ofNullable(this.comPortPool_id);
        Optional<InboundComPortPool> inboundComPortPool = Optional.empty();
        if (comPortPool_id.isPresent()) {
            inboundComPortPool=Optional.ofNullable(engineModelService.findInboundComPortPool(comPortPool_id.get()));
        }
        if(inboundComPortPool.isPresent()){
            builder.comPortPool(inboundComPortPool.get());
        }

        return builder;
    }

    @Override
    protected abstract ComPort createNew(ComServer comServer, EngineModelService engineModelService);
}
