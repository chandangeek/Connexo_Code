package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceGroupBuilder extends NamedBuilder<EndDeviceGroup, DeviceGroupBuilder> {
    private final MeteringGroupsService meteringGroupsService;
    private final DeviceConfigurationService deviceConfigurationService;

    private List<String> deviceTypes;

    @Inject
    public DeviceGroupBuilder(MeteringGroupsService meteringGroupsService, DeviceConfigurationService deviceConfigurationService) {
        super(DeviceGroupBuilder.class);
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceGroupBuilder withDeviceTypes(List<String> deviceTypes){
        this.deviceTypes = deviceTypes;
        return this;
    }

    @Override
    public Optional<EndDeviceGroup> find() {
        return meteringGroupsService.findEndDeviceGroupByName(getName());
    }

    @Override
    public EndDeviceGroup create() {
        Log.write(this);
        EndDeviceGroup endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(getCondition());
        endDeviceGroup.setName(getName());
        // dynamic
        endDeviceGroup.setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider");
        endDeviceGroup.save();
        return endDeviceGroup;
    }

    private Condition getCondition() {
        Condition condition = Condition.FALSE;
        if (deviceTypes == null){
            throw new UnableToCreate("You must specify the device types names for device group");
        }
        for (String deviceType : deviceTypes) {
            Optional<DeviceType> deviceTypeByName = deviceConfigurationService.findDeviceTypeByName(deviceType);
            if (!deviceTypeByName.isPresent()){
                throw new UnableToCreate("Unable to find device type with name " + deviceType);
            }
            condition = condition.or(where("deviceConfiguration.deviceType.id").isEqualTo(deviceTypeByName.get().getId()));
        }
        return condition.and(where("mRID").like(Constants.Device.STANDARD_PREFIX + "*"));
    }
}
