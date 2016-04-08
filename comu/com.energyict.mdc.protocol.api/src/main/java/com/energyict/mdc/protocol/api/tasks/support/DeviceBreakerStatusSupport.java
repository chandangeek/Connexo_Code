package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;

import aQute.bnd.annotation.ProviderType;

/**
 * Defines functionality related to Breaker status information
 */
@ProviderType
public interface DeviceBreakerStatusSupport {

    /**
     * @return the collected BreakerStatus
     */
    CollectedBreakerStatus getBreakerStatus();

}