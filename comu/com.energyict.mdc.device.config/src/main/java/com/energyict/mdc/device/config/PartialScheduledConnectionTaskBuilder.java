package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialConnectionTask;
import com.energyict.mdc.engine.model.OutboundComPortPool;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 14:36
 */
public interface PartialScheduledConnectionTaskBuilder<S, U extends PartialConnectionTask> extends PartialConnectionTaskBuilder<S, OutboundComPortPool, U> {

    NextExecutionSpecBuilder<S> nextExecutionSpec();
}
