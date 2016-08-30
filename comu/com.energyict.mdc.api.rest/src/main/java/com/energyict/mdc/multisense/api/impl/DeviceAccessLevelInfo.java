package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;

import java.util.List;

public class DeviceAccessLevelInfo extends LinkInfo<Long> {
    public String name;
    public List<PropertyInfo> properties;
}
