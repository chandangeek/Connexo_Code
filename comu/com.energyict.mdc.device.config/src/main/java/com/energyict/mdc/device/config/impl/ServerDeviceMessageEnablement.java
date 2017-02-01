/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;

public interface ServerDeviceMessageEnablement extends DeviceMessageEnablement{

    /**
     * Clones the current DeviceMessageEnablement for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned DeviceMessageEnablement
     * @return the cloned DeviceMessageEnablement
     */
    DeviceMessageEnablement cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);

}
