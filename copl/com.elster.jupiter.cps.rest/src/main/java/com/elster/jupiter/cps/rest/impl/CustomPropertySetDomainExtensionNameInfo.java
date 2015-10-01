package com.elster.jupiter.cps.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetDomainExtensionNameInfo {

    public String value;
    public String localizedValue;

    public CustomPropertySetDomainExtensionNameInfo() {
    }

    public CustomPropertySetDomainExtensionNameInfo(String value, String localizedValue) {
        this.value = value;
        this.localizedValue = localizedValue;
    }
}