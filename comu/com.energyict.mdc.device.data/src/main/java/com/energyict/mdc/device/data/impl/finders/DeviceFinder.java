package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.util.Optional;

/**
 * Provides an implementation for the {@link CanFindByLongPrimaryKey}
 * interface for {@link Device}s.
 *
 * Copyrights EnergyICT
 * Date: 27/03/14
 * Time: 16:46
 */
public class DeviceFinder implements CanFindByLongPrimaryKey<Device> {

    private final DeviceService deviceService;

    public DeviceFinder(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.DEVICE;
    }

    @Override
    public Class<Device> valueDomain() {
        return Device.class;
    }

    @Override

    public Optional<Device> findByPrimaryKey(long id) {
        return this.deviceService.findDeviceById(id);
    }

}