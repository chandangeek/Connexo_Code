/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface InboundConnectionTask extends ConnectionTask<InboundComPortPool, PartialInboundConnectionTask> {
}