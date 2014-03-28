package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Add behavior to {@link ServerDeviceConfiguration} that is
 * specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (15:28)
 */
public interface ServerDeviceConfiguration extends DeviceConfiguration {

    /**
     * Notifies this DeviceConfiguration that it is about to be deleted
     * as part of the delete of the {@link com.energyict.mdc.device.config.DeviceType}.
     */
    public void notifyDelete ();

    /**
     * Validates that none of the DeviceConfiguration constraints are violated by the
     * changes that have been applied to th LoadProfileType.
     * @param loadProfileType The LoadProfileType
     */
    public void validateUpdateLoadProfileType (LoadProfileType loadProfileType);

    /**
     * Validates that none of the DeviceConfiguration constraints are violated by the
     * changes that have been applied to th LogBookType.
     * @param logBookType The LogBookType
     */
    public void validateUpdateLogBookType (LogBookType logBookType);

    /**
     * Validates that none of the DeviceConfiguration constraints are violated by the
     * changes that have been applied to th RegisterMapping.
     * @param registerMapping The RegisterMapping
     */
    public void validateUpdateRegisterMapping(RegisterMapping registerMapping);

    /**
     * Prepares the device config for removal: i.e. clean all references to child records
     */
    public void prepareDelete();

}