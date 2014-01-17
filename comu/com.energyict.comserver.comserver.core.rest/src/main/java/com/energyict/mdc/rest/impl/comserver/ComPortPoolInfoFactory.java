package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;

public class ComPortPoolInfoFactory {
    public static ComPortPoolInfo<? extends ComPortPool> asInfo(ComPortPool comPortPool) {
        if (InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            return new InboundComPortPoolInfo((InboundComPortPool) comPortPool);
        } else {
            return new OutboundComPortPoolInfo((OutboundComPortPool) comPortPool);
        }
    }
}
