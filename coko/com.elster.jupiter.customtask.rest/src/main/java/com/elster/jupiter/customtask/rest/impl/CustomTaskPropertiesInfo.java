package com.elster.jupiter.customtask.rest.impl;


import com.elster.jupiter.properties.rest.PropertyInfo;

import java.util.List;

public class CustomTaskPropertiesInfo {

    public String name;
    public String displayName;
    public List<PropertyInfo> properties;

    public CustomTaskPropertiesInfo(String name, String displayName, List<PropertyInfo> properties) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
    }

    public CustomTaskPropertiesInfo() {
    }
}