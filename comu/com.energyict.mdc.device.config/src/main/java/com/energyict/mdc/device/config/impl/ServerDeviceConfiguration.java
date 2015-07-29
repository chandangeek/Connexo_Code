package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;

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
     * changes that have been applied to the RegisterType.
     * @param measurementType The RegisterType
     */
    public void validateUpdateMeasurementTypes(MeasurementType measurementType);

    /**
     * Prepares the device config for removal: i.e. clean all references to child records
     */
    public void prepareDelete();

    /**
     * Clones the current DeviceConfiguration and sets the given 'nameOfClone' as name for the cloned DeviceConfiguration
     *
     * @param nameOfClone the name of the cloned DeviceConfiguration
     * @return the cloned DeviceConfiguration
     */
    public DeviceConfiguration clone(String nameOfClone);

}