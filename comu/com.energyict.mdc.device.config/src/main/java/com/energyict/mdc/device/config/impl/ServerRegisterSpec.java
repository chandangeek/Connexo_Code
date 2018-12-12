/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.RegisterSpec;

public interface ServerRegisterSpec extends RegisterSpec{

    /**
     * Clones the current RegisterSpec for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned RegisterSpec
     * @return the cloned RegisterSpec
     */
    RegisterSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
