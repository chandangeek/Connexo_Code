/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingImpl;

import java.util.List;

/**
 * Adds behavior to {@link DeviceType} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-18 (11:23)
 */
public interface ServerDeviceType extends DeviceType {

    /**
     * Updates the {@link DeviceLifeCycle} and saves the changes.
     *
     * @param deviceLifeCycle The DeviceLifeCycle
     */
    void updateDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle);

    /**
     * Creates a new DeviceConfigConflictMappingImpl.
     *
     * @param origin the origin DeviceConfiguration
     * @param destination the destination DeviceConfiguration
     * @return a newly created DeviceConfigConflictMapping for this DeviceType with the given configs
     */
    DeviceConfigConflictMappingImpl newConflictMappingFor(DeviceConfiguration origin, DeviceConfiguration destination);

    /**
     * Remove the given deviceConfigConflictMappings
     *
     * @param deviceConfigConflictMappings the mappings to remove
     */
    void removeDeviceConfigConflictMappings(List<DeviceConfigConflictMapping> deviceConfigConflictMappings);

    /**
     * Cleans up the DeviceConfigConflictMapping which use the given partialConnectionTask
     *
     * @param partialConnectionTask the partialConnectionTask
     */
    void removeConflictsFor(PartialConnectionTask partialConnectionTask);

}