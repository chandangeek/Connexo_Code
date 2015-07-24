package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;

/**
 * Copyrights EnergyICT
 * Date: 13/07/15
 * Time: 11:34
 */
public interface ServerLoadProfileSpec extends LoadProfileSpec {

    /**
     * Clones the current LoadProfileSpec for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned LoadProfileSpec
     * @return the cloned LoadProfileSpec
     */
    LoadProfileSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
