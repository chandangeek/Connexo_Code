/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceMessageEnablement;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.MeasurementType;

import java.time.Instant;
import java.util.Optional;

/**
 * Add behavior to {@link ServerDeviceConfiguration} that is
 * specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (15:28)
 */
interface ServerDeviceConfiguration extends DeviceConfiguration {

    /**
     * Notifies this DeviceConfiguration that it is about to be deleted
     * as part of the delete of the {@link DeviceType}.
     */
    void notifyDelete();

    /**
     * Validates that none of the DeviceConfiguration constraints are violated by the
     * changes that have been applied to th LoadProfileType.
     * @param loadProfileType The LoadProfileType
     */
    void validateUpdateLoadProfileType(LoadProfileType loadProfileType);

    /**
     * Validates that none of the DeviceConfiguration constraints are violated by the
     * changes that have been applied to th LogBookType.
     * @param logBookType The LogBookType
     */
    void validateUpdateLogBookType(LogBookType logBookType);

    /**
     * Validates that none of the DeviceConfiguration constraints are violated by the
     * changes that have been applied to the RegisterType.
     * @param measurementType The RegisterType
     */
    void validateUpdateMeasurementTypes(MeasurementType measurementType);

    /**
     * Prepares the device config for removal: i.e. clean all references to child records
     */
    void prepareDelete();

    boolean isObsolete();

    Optional<Instant> getObsoleteDate();

    void makeObsolete();
    /**
     * Clones the current DeviceConfiguration and sets the given 'nameOfClone' as name for the cloned DeviceConfiguration
     *
     * @param nameOfClone the name of the cloned DeviceConfiguration
     * @return the cloned DeviceConfiguration
     */
    DeviceConfiguration clone(String nameOfClone);

    /**
     * Notifies this DeviceConfiguration that file management has been disabled
     * on the owning DeviceType and that all {@link DeviceMessageEnablement}s
     * that relate to file management should be cleaned up.
     */
    void fileManagementDisabled();

}