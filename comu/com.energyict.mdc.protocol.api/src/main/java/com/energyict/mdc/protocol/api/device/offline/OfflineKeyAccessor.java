package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;

public interface OfflineKeyAccessor<T extends SecurityValueWrapper> extends Offline {

    /**
     * Returns the Id of the Device which owns this SecurityAccessor.
     *
     * @return the {@link OfflineDevice}
     */
    int getDeviceId();

    /**
     * Get the KeyAccessorType this value belongs to
     */
    KeyAccessorType getKeyAccessorType();

    /**
     * The actual value is the value to be used at present
     * @return The current value
     */
    T getActualValue();

    /**
     * Whenever this security element is in the process of being renewed. The future value is saved in the temp field.
     * Casual users should not need to access this value, it is stored for the renew process.
     * @return The value as generated for the renew process.
     */
    Optional<T> getTempValue();

    /**
     * The identifier that uniquely identifies the device.
     *
     * @return the deviceIdentifier
     */
    DeviceIdentifier<?> getDeviceIdentifier();

}
