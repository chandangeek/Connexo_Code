package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.DeviceGroupBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link Template} holding a set of predefined attributes for creating {@link EndDeviceGroup}s
 *
 * Copyrights EnergyICT
 * Date: 17/09/2015
 * Time: 9:56
 */
public enum DeviceGroupTpl implements Template<EndDeviceGroup, DeviceGroupBuilder> {
    NORTH_REGION("South region", Arrays.asList(
            DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000
    )),
    SOUTH_REGION("North region", Arrays.asList(
            DeviceTypeTpl.Elster_A1800, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38
    )),
    ALL_ELECTRICITY_DEVICES("All electricity devices", Arrays.asList(
            DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000,
            DeviceTypeTpl.Elster_A1800, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38
    )),
    ;

    private String name;
    private List<DeviceTypeTpl> deviceTypes;

    DeviceGroupTpl(String name, List<DeviceTypeTpl> deviceTypes) {
        this.name = name;
        this.deviceTypes = deviceTypes;
    }

    @Override
    public Class<DeviceGroupBuilder> getBuilderClass() {
        return DeviceGroupBuilder.class;
    }

    @Override
    public DeviceGroupBuilder get(DeviceGroupBuilder builder) {
        return builder.withName(this.name).withDeviceTypes(this.deviceTypes.stream().map(DeviceTypeTpl::getName).collect(Collectors.toList()));
    }

    public String getName() {
        return name;
    }
}
