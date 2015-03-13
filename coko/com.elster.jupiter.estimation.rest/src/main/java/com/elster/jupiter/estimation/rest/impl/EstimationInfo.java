package com.elster.jupiter.estimation.rest.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

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
