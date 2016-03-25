package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertiesInfo {
    public String key;
    public String name;
    public IdWithNameInfo customPropertySet;

    public CustomPropertiesInfo() {
    }
}
