/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.energyict.mdc.common.tasks.PartialConnectionTask;

public interface ServerPartialConnectionTask extends PartialConnectionTask {

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
