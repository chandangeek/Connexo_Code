/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;

interface ServerPartialConnectionTask extends PartialConnectionTask {

    DeleteEventType deleteEventType();

    void validateDelete();

    void prepareDelete();

    void clearDefault();

    /**
     * Clones the current PartialConnectionTask for the given DeviceConfiguration
     *
     * @param deviceConfiguration the owner of the cloned PartialConnectionTask
     * @return the cloned PartialConnectionTask
     */
    PartialConnectionTask cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);
}
