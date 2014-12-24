package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import java.util.Optional;


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
    protected void writeTo(T source,EngineConfigurationService engineConfigurationService) {
        super.writeTo(source, engineConfigurationService);
        Optional<Long> comPortPool_id = Optional.ofNullable(this.comPortPool_id);
        Optional<InboundComPortPool> inboundComPortPool;
        if (comPortPool_id.isPresent()) {
            inboundComPortPool = engineConfigurationService.findInboundComPortPool(comPortPool_id.get());
            if (inboundComPortPool.isPresent()) {
                source.setComPortPool(inboundComPortPool.get());
            }
        }
    }

    @Override
    protected B build(B builder, EngineConfigurationService engineConfigurationService) {
        super.build(builder, engineConfigurationService);
        Optional<Long> comPortPool_id = Optional.ofNullable(this.comPortPool_id);
        Optional<InboundComPortPool> inboundComPortPool = Optional.empty();
        if (comPortPool_id.isPresent()) {
            inboundComPortPool = engineConfigurationService.findInboundComPortPool(comPortPool_id.get());
        }
        if (inboundComPortPool.isPresent()) {
            builder.comPortPool(inboundComPortPool.get());
        }

        return builder;
    }

    @Override
    protected abstract ComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService);

}