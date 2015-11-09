package com.energyict.mdc.device.data.impl.finders;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.orm.DataModel;

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

    private final DataModel dataModel;

    public DeviceFinder(DataModel dataModel) {
        this.dataModel = dataModel;
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
        return this.dataModel.mapper(Device.class).getUnique("id", id);
    }

}