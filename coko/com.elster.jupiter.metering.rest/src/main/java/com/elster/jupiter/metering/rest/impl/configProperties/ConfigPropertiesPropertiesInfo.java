/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.configProperties;

import com.elster.jupiter.properties.rest.PropertyInfo;

import java.util.List;

public class ConfigPropertiesPropertiesInfo {
    public String name;
    public String displayName;
    public List<PropertyInfo> properties;

    public ConfigPropertiesPropertiesInfo(String name, String displayName, List<PropertyInfo> properties) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
    }

    public ConfigPropertiesPropertiesInfo() {
    }
}
