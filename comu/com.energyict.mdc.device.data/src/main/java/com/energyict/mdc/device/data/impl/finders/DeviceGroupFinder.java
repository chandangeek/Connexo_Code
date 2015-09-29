package com.energyict.mdc.device.data.impl.finders;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;

import java.util.Optional;

public class DeviceGroupFinder implements CanFindByLongPrimaryKey<EndDeviceGroup> {

    private MeteringGroupsService meteringGroupsService;

    public DeviceGroupFinder(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.DEVICE_GROUP;
    }

    @Override
    public Class<EndDeviceGroup> valueDomain() {
        return EndDeviceGroup.class;
    }

    @Override
    public Optional<EndDeviceGroup> findByPrimaryKey(long id) {
        return meteringGroupsService.findEndDeviceGroup(id);
    }
}
