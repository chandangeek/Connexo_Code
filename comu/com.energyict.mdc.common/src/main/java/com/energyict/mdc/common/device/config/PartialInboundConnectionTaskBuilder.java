/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.comserver.InboundComPortPool;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartialInboundConnectionTaskBuilder extends PartialConnectionTaskBuilder<PartialInboundConnectionTaskBuilder, InboundComPortPool, PartialInboundConnectionTask> {

    PartialInboundConnectionTaskBuilder asDefault(boolean asDefault);

}
