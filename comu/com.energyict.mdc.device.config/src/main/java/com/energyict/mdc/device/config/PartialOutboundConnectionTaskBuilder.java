package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.OutboundComPortPool;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 14:36
 */
@ProviderType
public interface PartialOutboundConnectionTaskBuilder<S, U extends com.energyict.mdc.device.config.PartialConnectionTask> extends PartialConnectionTaskBuilder<S, OutboundComPortPool, U> {

    NextExecutionSpecBuilder<S> nextExecutionSpec();

    S rescheduleDelay(TimeDuration duration);
}
