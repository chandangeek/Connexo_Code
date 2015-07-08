package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Copyrights EnergyICT
 * Date: 08/07/15
 * Time: 15:52
 */
public interface ServerComTaskEnablement extends ComTaskEnablement {

    /**
     * Clones the current ComTaskEnablement for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned ComTaskEnablement
     * @return the cloned ComTaskEnablement
     */
    ComTaskEnablement cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
