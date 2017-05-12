/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;

import java.util.List;

/**
 * Adds behavior to {@link com.energyict.mdc.device.data.Device} that is specific
 * to server side components.
 */
public interface ServerDeviceForConfigChange extends Device {

    String CONFIG_CHANGE_BULK_QUEUE_DESTINATION = "ConfigChangeBulkQD";
    String DEVICE_CONFIG_CHANGE_SUBSCRIBER = "DeviceConfigChangeSubscriber";
    String DEVICE_CONFIG_CHANGE_SUBSCRIBER_DISPLAY_NAME = "Handle bulk actions for device configuration changes";

    String CONFIG_CHANGE_MESSAGE_VALUE = "ConfigChangeMessageValue";
    String DEVICE_CONFIG_CHANGE_BULK_SETUP_ACTION = "deviceConfigChange/SETUP";
    String DEVICE_CONFIG_CHANGE_SINGLE_START_ACTION = "deviceConfigChange/START";
    String DEVICE_CONFIG_CHANGE_SINGLE_COMPLETED_ACTION = "deviceConfigChange/COMPLETED";

    /**
     * Validates whether or not this device can change its configuration to the given destinationDeviceConfig
     *
     * @param destinationDeviceConfig the config to check
     */
    void validateDeviceCanChangeConfig(DeviceConfiguration destinationDeviceConfig);

    void setNewDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    void removeLoadProfiles(List<LoadProfile> loadProfiles);

    void addLoadProfiles(List<LoadProfileSpec> loadProfileSpecs);

    void removeLogBooks(List<LogBook> logBooks);

    void addLogBooks(List<LogBookSpec> logBookSpecs);

}
