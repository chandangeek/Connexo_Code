package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import java.util.Optional;

/**
 * Provides an implementation for the {@link CanFindByLongPrimaryKey}
 * interface for {@link DeviceType}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-29 (13:50)
 */
public class DeviceTypeFinder implements CanFindByLongPrimaryKey<DeviceType> {

    private final DeviceConfigurationService deviceConfigurationService;

    public DeviceTypeFinder(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.DEVICE_TYPE;
    }

    @Override
    public Class<DeviceType> valueDomain() {
        return DeviceType.class;
    }

    @Override
    public Optional<DeviceType> findByPrimaryKey(long id) {
        return this.deviceConfigurationService.findDeviceType(id);
    }


}