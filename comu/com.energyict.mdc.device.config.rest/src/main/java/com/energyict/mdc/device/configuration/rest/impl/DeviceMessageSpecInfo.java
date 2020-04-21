/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.upl.TypedProperties;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceMessageSpecInfo {
    public static final String NOT_SET = "NOT_SET";

    public String id;
    public String name;
    public List<PropertyInfo> properties;

    public static DeviceMessageSpecInfo from(DeviceMessageSpec deviceMessageSpec, MdcPropertyUtils mdcPropertyUtils){
        DeviceMessageSpecInfo deviceMessageSpecInfo = new DeviceMessageSpecInfo();
        deviceMessageSpecInfo.id = deviceMessageSpec.getId().name();
        deviceMessageSpecInfo.name = deviceMessageSpec.getName();
        List<PropertySpec> propertySpecs =
            deviceMessageSpec.getPropertySpecs().stream()
                    .filter(propertySpec -> !(propertySpec.getValueFactory()!= null && propertySpec.getValueFactory().getValueType().getName().equals("com.elster.jupiter.pki.SecurityAccessorType")))
                    .collect(Collectors.toList());
        deviceMessageSpecInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, TypedProperties.empty());
        return deviceMessageSpecInfo;
    }

    public static DeviceMessageSpecInfo getNotSetDeviceMessageSpecInfo() {
        DeviceMessageSpecInfo deviceMessageSpecInfo = new DeviceMessageSpecInfo();
        deviceMessageSpecInfo.id = NOT_SET;
        deviceMessageSpecInfo.name = "Not set";
        deviceMessageSpecInfo.properties = Collections.emptyList();
        return deviceMessageSpecInfo;
    }
}
