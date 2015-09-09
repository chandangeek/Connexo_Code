package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

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
     * Updates the DeviceConfigConflictMappings
     */
    void updateConflictingMappings();

    /**
     * Creates a new DeviceConfigConflictMapping.
     *
     * @param origin the origin DeviceConfiguration
     * @param destination the destination DeviceConfiguration
     * @return a newly created DeviceConfigConflictMapping for this DeviceType with the given configs
     */
    DeviceConfigConflictMapping newConflictMappingFor(DeviceConfiguration origin, DeviceConfiguration destination);

    /**
     * Cleans up the DeviceConfigConflictMapping which use the given partialConnectionTask
     *
     * @param partialConnectionTask the partialConnectionTask
     */
    void removeConflictsFor(PartialConnectionTask partialConnectionTask);

    /**
     * Cleans up the DeviceConfigConflictMapping which uses the given SecurityPropertySet
     *
     * @param securityPropertySet the SecurityPropertySet
     */
    void removeConflictsFor(SecurityPropertySet securityPropertySet);
}