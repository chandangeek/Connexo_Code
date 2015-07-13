package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;

/**
 * Copyrights EnergyICT
 * Date: 09/07/15
 * Time: 15:47
 */
public interface ServerDeviceMessageEnablement extends DeviceMessageEnablement{

    /**
     * Clones the current DeviceMessageEnablement for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned DeviceMessageEnablement
     * @return the cloned DeviceMessageEnablement
     */
    DeviceMessageEnablement cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);

}
