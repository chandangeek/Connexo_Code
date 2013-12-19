package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.shadow.ports.ComPortPoolShadow;

public class ComPortPoolInfoFactory {
    public static ComPortPoolInfo<? extends ComPortPoolShadow> asInfo(ComPortPool comPortPool) {
        if (InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            return new InboundComPortPoolInfo((InboundComPortPool) comPortPool);
        } else {
            return new OutboundComPortPoolInfo((OutboundComPortPool) comPortPool);
        }
    }
}
