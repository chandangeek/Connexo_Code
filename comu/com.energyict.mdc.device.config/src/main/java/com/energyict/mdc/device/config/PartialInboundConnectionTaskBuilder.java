/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialInboundConnectionTaskImpl;
import com.energyict.mdc.engine.config.InboundComPortPool;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartialInboundConnectionTaskBuilder extends PartialConnectionTaskBuilder<PartialInboundConnectionTaskBuilder, InboundComPortPool, PartialInboundConnectionTaskImpl> {

    PartialInboundConnectionTaskBuilder asDefault(boolean asDefault);

}
