package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialInboundConnectionTaskImpl;
import com.energyict.mdc.engine.config.InboundComPortPool;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 11:42
 */
public interface PartialInboundConnectionTaskBuilder extends PartialConnectionTaskBuilder<PartialInboundConnectionTaskBuilder, InboundComPortPool, PartialInboundConnectionTaskImpl> {

    PartialInboundConnectionTaskBuilder asDefault(boolean asDefault);

}
