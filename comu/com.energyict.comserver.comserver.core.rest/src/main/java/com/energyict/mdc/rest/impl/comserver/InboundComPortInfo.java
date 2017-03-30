/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;

import java.util.Optional;


public abstract class InboundComPortInfo<T extends InboundComPort, B extends InboundComPort.InboundComPortBuilder<B, T>> extends ComPortInfo<T, B> {

    public VersionInfo<Long> comPortPool_id;

    protected InboundComPortInfo() {
        this.direction = "inbound";
    }

    public InboundComPortInfo(InboundComPort comPort) {
        super(comPort);
        this.direction = "inbound";
        Optional<InboundComPortPool> comPortPool = Optional.ofNullable(comPort.getComPortPool());
        if (comPortPool.isPresent()) {
            this.comPortPool_id = new VersionInfo<>(comPort.getComPortPool().getId(), comPort.getComPortPool().getVersion());
        }
    }

    @Override
    protected void writeTo(T source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        if (this.comPortPool_id != null && this.comPortPool_id.id != null) {
            ComPortPool comPortPool = resourceHelper.getLockedComPortPool(this.comPortPool_id.id, this.comPortPool_id.version)
                    .orElseThrow(resourceHelper.getConcurrentExSupplier(this.name, () -> resourceHelper.getCurrentComPortVersion(this.id)));
            source.setComPortPool((InboundComPortPool)comPortPool);
        }
    }

    @Override
    protected B build(B builder, EngineConfigurationService engineConfigurationService) {
        super.build(builder, engineConfigurationService);
        Optional<InboundComPortPool> inboundComPortPool = Optional.empty();
        if (this.comPortPool_id != null && this.comPortPool_id.id != null) {
            inboundComPortPool = engineConfigurationService.findInboundComPortPool(comPortPool_id.id);
        }
        if (inboundComPortPool.isPresent()) {
            builder.comPortPool(inboundComPortPool.get());
        }

        return builder;
    }

    @Override
    protected abstract ComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService);

}