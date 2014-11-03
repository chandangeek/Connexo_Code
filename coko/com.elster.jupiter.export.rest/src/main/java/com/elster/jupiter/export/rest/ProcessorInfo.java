package com.elster.jupiter.export.rest;


import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ProcessorInfo {

    public String name;
    public String displayName;
    public List<PropertyInfo> properties;

    public ProcessorInfo(String name, String displayName, List<PropertyInfo> properties) {
        this.name = name;
        this.displayName = displayName;
        this.properties = properties;
    }

    public ProcessorInfo() {
    }
}