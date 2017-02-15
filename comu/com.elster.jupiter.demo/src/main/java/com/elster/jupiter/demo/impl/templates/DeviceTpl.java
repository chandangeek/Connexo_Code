/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.energyict.mdc.device.data.Device;

//TODO write device templates
public class DeviceTpl implements Template<Device, DeviceBuilder>{

    private String namePrefix;
    private String serialNumber;
    private int year;
    private boolean validationActive;

    @Override
    public Class<DeviceBuilder> getBuilderClass() {
        return DeviceBuilder.class;
    }

    @Override
    public DeviceBuilder get(DeviceBuilder builder) {
        return builder.withName(this.namePrefix + this.serialNumber).withSerialNumber(serialNumber).withYearOfCertification(year);
    }
}
