/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.device.configuration.rest.impl.DeviceConfigurationInfo;

import java.util.List;

public class ProtocolInfo {

    public long id;
    public String name;
    public List<PropertyInfo> properties;
    public DeviceConfigurationInfo deviceConfiguration;

    public ProtocolInfo() {
    }

}
