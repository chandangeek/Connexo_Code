package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;

/**
 * Copyrights EnergyICT
 * Date: 08/07/15
 * Time: 14:48
 */
public interface ServerPartialConnectionTask extends PartialConnectionTask {

    /**
     * Clones the current PartialConnectionTask for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned PartialConnectionTask
     * @return the cloned PartialConnectionTask
     */
    PartialConnectionTask cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
