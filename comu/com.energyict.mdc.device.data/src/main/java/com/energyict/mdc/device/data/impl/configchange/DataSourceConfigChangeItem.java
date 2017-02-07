/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Provides functionality to handle certain DataSources (DS) when a Device changes his DeviceConfiguration
 */
public interface DataSourceConfigChangeItem {

    /**
     * Applies the actual change for a DataSource when a device changes its DeviceConfiguration
     *
     * @param device                         the device that changes its configuration
     * @param originDeviceConfiguration      the original DeviceConfiguration
     * @param destinationDeviceConfiguration the destination DeviceConfiguration
     */
    void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration);
}
