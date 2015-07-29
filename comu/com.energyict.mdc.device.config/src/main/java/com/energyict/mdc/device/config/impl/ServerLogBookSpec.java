package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;

/**
 * Copyrights EnergyICT
 * Date: 13/07/15
 * Time: 11:21
 */
public interface ServerLogBookSpec extends LogBookSpec{

    /**
     * Clones the current LogBookSpec for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned LogBookSpec
     * @return the cloned LogBookSpec
     */
    LogBookSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
