package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingTypePatternInfo {
    public String value;
    public ReadingTypePatternAttributeInfo attributes;

    public ReadingTypePatternInfo() {
    }
}