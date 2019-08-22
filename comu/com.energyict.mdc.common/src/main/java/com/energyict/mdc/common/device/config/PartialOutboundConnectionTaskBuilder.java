/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface PartialOutboundConnectionTaskBuilder<S, U extends PartialConnectionTask> extends PartialConnectionTaskBuilder<S, OutboundComPortPool, U> {

    NextExecutionSpecBuilder<S> nextExecutionSpec();

    S rescheduleDelay(TimeDuration duration);
}
