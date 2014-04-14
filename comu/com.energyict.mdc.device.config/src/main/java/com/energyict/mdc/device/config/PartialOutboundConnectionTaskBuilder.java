package com.energyict.mdc.device.config;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.OutboundComPortPool;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 14:36
 */
public interface PartialOutboundConnectionTaskBuilder<S, U extends com.energyict.mdc.device.config.PartialConnectionTask> extends PartialConnectionTaskBuilder<S, OutboundComPortPool, U> {

    NextExecutionSpecBuilder<S> nextExecutionSpec();

    S rescheduleDelay(TimeDuration duration);
}
