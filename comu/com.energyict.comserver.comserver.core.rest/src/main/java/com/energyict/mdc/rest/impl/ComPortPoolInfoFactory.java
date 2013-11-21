package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPortPool;
import com.energyict.mdc.ports.InboundComPortPool;
import com.energyict.mdc.ports.OutboundComPortPool;
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
