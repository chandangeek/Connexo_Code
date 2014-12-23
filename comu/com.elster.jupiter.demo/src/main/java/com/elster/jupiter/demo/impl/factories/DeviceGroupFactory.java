package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceGroupFactory extends NamedFactory<DeviceGroupFactory, EndDeviceGroup> {
    private final MeteringGroupsService meteringGroupsService;
    private final Store store;

    private String[] deviceTypes;

    @Inject
    public DeviceGroupFactory(Store store, MeteringGroupsService meteringGroupsService) {
        super(DeviceGroupFactory.class);
        this.store = store;
        this.meteringGroupsService = meteringGroupsService;
    }

    public DeviceGroupFactory withDeviceTypes(String... deviceTypes){
        this.deviceTypes = deviceTypes;
        return this;
    }

    public EndDeviceGroup get(){
        Log.write(this);
        Optional<EndDeviceGroup> groupByName = meteringGroupsService.findEndDeviceGroupByName(getName());
        if (groupByName.isPresent()) {
            store.add(EndDeviceGroup.class, groupByName.get());
        } else {
            EndDeviceGroup endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(getCondition());
            endDeviceGroup.setName(getName());
            // dynamic
            endDeviceGroup.setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider");
            endDeviceGroup.save();
            groupByName = Optional.of(endDeviceGroup);
        }
        store.add(EndDeviceGroup.class, groupByName.get());
        return groupByName.get();
    }

    private Condition getCondition() {
        Condition condition = Condition.FALSE;
        if (deviceTypes == null){
            throw new UnableToCreate("You must specify the device types names for device group");
        }
        for (String deviceType : deviceTypes) {
            condition = condition.or(where("deviceConfiguration.deviceType.name").isEqualTo(deviceType));
        }
        return condition;
    }
}
