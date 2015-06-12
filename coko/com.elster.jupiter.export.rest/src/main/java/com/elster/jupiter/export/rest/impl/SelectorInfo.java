package com.elster.jupiter.export.rest.impl;


import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class SelectorInfo {

    public String name;
    public String displayName;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public boolean isDefault;



    public SelectorInfo(String name, String displayName, List<PropertyInfo> properties, boolean isDefault) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
        this.isDefault = isDefault;
    }

    public SelectorInfo(String name, String displayName, boolean isDefault) {
        this.name = name;
        this.displayName = displayName;
        this.isDefault = isDefault;
    }

    public SelectorInfo() {
    }
}