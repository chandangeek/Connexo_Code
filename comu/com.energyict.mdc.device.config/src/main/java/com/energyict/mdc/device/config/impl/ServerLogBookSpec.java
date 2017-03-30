/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;

public interface ServerLogBookSpec extends LogBookSpec{

    /**
     * Clones the current LogBookSpec for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned LogBookSpec
     * @return the cloned LogBookSpec
     */
    LogBookSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
