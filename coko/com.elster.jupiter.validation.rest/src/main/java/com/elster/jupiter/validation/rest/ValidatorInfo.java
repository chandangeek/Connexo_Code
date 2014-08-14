package com.elster.jupiter.validation.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

@XmlRootElement
public class ValidatorInfo {

    public String implementation;
    public String displayName;
    public List<PropertyInfo> properties;

    public ValidatorInfo(String implementation, String displayName, List<PropertyInfo> properties) {
        this.implementation = implementation;
        this.displayName = displayName;
        this.properties = properties;
    }

    public ValidatorInfo() {
    }
}
