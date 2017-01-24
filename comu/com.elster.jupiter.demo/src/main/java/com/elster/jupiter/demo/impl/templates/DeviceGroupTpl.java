package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.DeviceGroupBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * {@link Template} holding a set of predefined attributes for creating {@link EndDeviceGroup}s
 * <p>
 * Copyrights EnergyICT
 * Date: 17/09/2015
 * Time: 9:56
 */
public enum DeviceGroupTpl implements Template<EndDeviceGroup, DeviceGroupBuilder> {
    NORTH_REGION("North region", Constants.Device.STANDARD_PREFIX + "*",
            DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000
    ),
    SOUTH_REGION("South region", Constants.Device.STANDARD_PREFIX + "*",
            DeviceTypeTpl.Elster_A1800, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38
    ),
    ALL_ELECTRICITY_DEVICES("All electricity devices", null,
            DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000,
            DeviceTypeTpl.Elster_A1800, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38
    ),
    A1800_DEVICES("Elster A1800 devices", null, DeviceTypeTpl.Elster_A1800),
    DATA_LOGGERS("Data loggers", null, DeviceTypeTpl.WEBRTU_Z2),
    DATA_LOGGER_SLAVES("Data logger slaves", null, DeviceTypeTpl.EIMETER_FLEX);

    private String name;
    private String namePrefix;
    private DeviceTypeTpl[] deviceTypes;

    DeviceGroupTpl(String name, String namePrefix, DeviceTypeTpl... deviceTypes) {
        this.name = name;
        this.namePrefix = namePrefix;
        this.deviceTypes = deviceTypes;
    }

    @Override
    public Class<DeviceGroupBuilder> getBuilderClass() {
        return DeviceGroupBuilder.class;
    }

    @Override
    public DeviceGroupBuilder get(DeviceGroupBuilder builder) {
        return builder.withName(this.name).withNamePrefix(namePrefix).withDeviceTypes(Arrays.stream(this.deviceTypes).map(DeviceTypeTpl::getName).collect(Collectors.toList()));
    }

    public String getName() {
        return name;
    }
}
