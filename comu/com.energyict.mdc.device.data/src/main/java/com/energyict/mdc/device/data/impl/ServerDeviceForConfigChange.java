package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.dynamic.relation.CanLock;

import java.util.List;

/**
 * Adds behavior to {@link com.energyict.mdc.device.data.Device} that is specific
 * to server side components.
 */
interface ServerDeviceForConfigChange extends Device, CanLock {

    /**
     * Validates whether or not this device can change its configuration to the given destinationDeviceConfig
     *
     * @param destinationDeviceConfig the config to check
     */
    void validateDeviceCanChangeConfig(DeviceConfiguration destinationDeviceConfig);

    void setNewDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    void createNewMeterActivation();

    void removeLoadProfiles(List<LoadProfile> loadProfiles);

    void addLoadProfiles(List<LoadProfileSpec> loadProfileSpecs);

    void removeLogBooks(List<LogBook> logBooks);

    void addLogBooks(List<LogBookSpec> logBookSpecs);
}
