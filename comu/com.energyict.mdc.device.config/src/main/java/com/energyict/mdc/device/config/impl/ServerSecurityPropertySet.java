package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;

/**
 * Provides server side functionality regarding a SecurityPropertySet
 */
public interface ServerSecurityPropertySet extends SecurityPropertySet {

    /**
     * Clones the current SecurityPropertySet
     *
     * @param deviceConfiguration the DeviceConfiguration for which the cloned SecurityPropertySet must be created
     * @return the cloned SecurityPropertySet
     */
    SecurityPropertySet cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
