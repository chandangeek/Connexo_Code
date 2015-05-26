package com.elster.jupiter.export.rest.impl;


import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SelectorInfo {

    public String name;
    public String displayName;
    public List<PropertyInfo> properties;

    public SelectorInfo(String name, String displayName, List<PropertyInfo> properties) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
    }

    public SelectorInfo() {
    }
}