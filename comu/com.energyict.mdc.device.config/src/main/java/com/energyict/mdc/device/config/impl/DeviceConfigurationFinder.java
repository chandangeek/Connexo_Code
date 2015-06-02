package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import java.util.Optional;

/**
 * Provides an implementation for the {@link CanFindByLongPrimaryKey}
 * interface for {@link DeviceConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (08:40)
 */
public class DeviceConfigurationFinder implements CanFindByLongPrimaryKey<DeviceConfiguration> {

    private final DeviceConfigurationService deviceConfigurationService;

    public DeviceConfigurationFinder(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.DEVICE_CONFIGURATION;
    }

    @Override
    public Class<DeviceConfiguration> valueDomain() {
        return DeviceConfiguration.class;
    }

    @Override
    public Optional<DeviceConfiguration> findByPrimaryKey(long id) {
        return this.deviceConfigurationService.findDeviceConfiguration(id);
    }

}