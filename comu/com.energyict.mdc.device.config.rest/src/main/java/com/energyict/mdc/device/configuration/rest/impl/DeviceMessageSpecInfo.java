/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;

import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.TypedProperties;

import java.util.List;

public class DeviceMessageSpecInfo {
    public String id;
    public String name;
    public List<PropertyInfo> properties;

    public static DeviceMessageSpecInfo from(DeviceMessageSpec deviceMessageSpec, MdcPropertyUtils mdcPropertyUtils){
        DeviceMessageSpecInfo deviceMessageSpecInfo = new DeviceMessageSpecInfo();
        deviceMessageSpecInfo.id = deviceMessageSpec.getId().name();
        deviceMessageSpecInfo.name = deviceMessageSpec.getName();
        deviceMessageSpecInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceMessageSpec.getPropertySpecs(), TypedProperties.empty());
        return deviceMessageSpecInfo;
    }
}
