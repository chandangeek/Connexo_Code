package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class EstimationInfo {

    public String implementation;
    public String displayName;
    public List<PropertyInfo> properties;

    public EstimationInfo(String implementation, String displayName, List<PropertyInfo> properties) {
        this.implementation = implementation;
        this.displayName = displayName;
        this.properties = properties;
    }

    public EstimationInfo() {
    }
}
