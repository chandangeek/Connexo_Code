package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;

import java.util.Optional;

/**
 * Defines functionality related to Breaker status information
 */
@ProviderType
public interface DeviceBreakerStatusSupport {

    /**
     * @return the collected BreakerStatus<br/>
     * Note: if the {@link DeviceProtocol} doesn't support breaker functionality (e.g. the device is
     * not equipped with a breaker), then as an implementor you still should return a
     * valid {@link CollectedBreakerStatus} but without actual breaker status
     * (or in other words: {@link CollectedBreakerStatus#getBreakerStatus()} in this case should
     * still refer to {@link Optional#empty()})
     */
    CollectedBreakerStatus getBreakerStatus();

}