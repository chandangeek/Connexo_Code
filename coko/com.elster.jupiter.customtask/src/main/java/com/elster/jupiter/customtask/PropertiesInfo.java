package com.elster.jupiter.customtask;


import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

public class PropertiesInfo {

    public String name;
    public String displayName;
    public List<PropertySpec> properties;

    public PropertiesInfo(String name, String displayName, List<PropertySpec> properties) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
    }

    public PropertiesInfo() {
    }

    public String getName(){
        return name;
    }

    public String getDisplayName(){
        return displayName;
    }

    public List<PropertySpec> getProperties(){
        return properties;
    }

}