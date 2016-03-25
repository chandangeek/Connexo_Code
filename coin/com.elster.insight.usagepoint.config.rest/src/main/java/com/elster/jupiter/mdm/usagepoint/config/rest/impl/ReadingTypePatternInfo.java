package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingTypePatternInfo {
    public String value;
    public ReadingTypePatternAttributesInfo attributes;

    public ReadingTypePatternInfo() {
    }
}