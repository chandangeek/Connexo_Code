package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;

public class ComPortPoolInfoFactory {
    public static ComPortPoolInfo<? extends ComPortPool> asInfo(ComPortPool comPortPool, EngineConfigurationService engineConfigurationService) {
        if (InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            return new InboundComPortPoolInfo((InboundComPortPool) comPortPool);
        } else {
            return new OutboundComPortPoolInfo((OutboundComPortPool) comPortPool, engineConfigurationService);
        }
    }
}
