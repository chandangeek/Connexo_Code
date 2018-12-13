/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.VersionInfo;

import java.util.List;

public class DeviceProtocolInfo {

    public long id;
    public String name;
    public List<PropertyInfo> properties;
    public long version;
    public VersionInfo<String> parent;

    public DeviceProtocolInfo() {
    }

}
