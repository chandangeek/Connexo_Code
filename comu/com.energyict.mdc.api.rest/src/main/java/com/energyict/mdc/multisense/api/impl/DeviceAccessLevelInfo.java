package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class DeviceAccessLevelInfo extends LinkInfo<Long> {
    public String name;
    public List<PropertyInfo> properties;
}
