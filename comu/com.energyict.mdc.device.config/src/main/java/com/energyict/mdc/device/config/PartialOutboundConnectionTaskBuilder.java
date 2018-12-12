/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.OutboundComPortPool;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartialOutboundConnectionTaskBuilder<S, U extends com.energyict.mdc.device.config.PartialConnectionTask> extends PartialConnectionTaskBuilder<S, OutboundComPortPool, U> {

    NextExecutionSpecBuilder<S> nextExecutionSpec();

    S rescheduleDelay(TimeDuration duration);
}
