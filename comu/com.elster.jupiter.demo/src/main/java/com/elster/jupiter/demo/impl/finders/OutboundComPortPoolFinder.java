package com.elster.jupiter.demo.impl.finders;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;

import javax.inject.Inject;

public class OutboundComPortPoolFinder extends NamedFinder<OutboundComPortPoolFinder, OutboundComPortPool> {

    private final EngineConfigurationService engineConfigurationService;

    @Inject
    public OutboundComPortPoolFinder(EngineConfigurationService engineConfigurationService) {
        super(OutboundComPortPoolFinder.class);
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    public OutboundComPortPool find() {
        return engineConfigurationService.findOutboundComPortPoolByName(getName()).orElseThrow(getFindException());
    }
}
