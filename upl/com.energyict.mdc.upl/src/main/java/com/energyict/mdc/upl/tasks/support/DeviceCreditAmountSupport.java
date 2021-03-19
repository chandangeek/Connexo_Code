package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;

/**
 * Defines functionality related to Breaker status information
 */
@ProviderType
public interface DeviceCreditAmountSupport {

    /**
     * @return the collected CreditAmount<br/>
     * Note: if the {@link DeviceProtocol} doesn't support the functionality then as an implementor
     * you still should return a valid {@link CollectedCreditAmount} but with the actual credit amount 0.
     */
    default CollectedCreditAmount getCreditAmount() {
        return null;
    }

}