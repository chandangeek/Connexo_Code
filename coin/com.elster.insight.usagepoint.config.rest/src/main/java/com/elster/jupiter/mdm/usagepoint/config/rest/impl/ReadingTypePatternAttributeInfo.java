package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingTypePatternAttributeInfo {

    public List<String> timePeriod;
    public List<String> accumulation;
    public List<String> multiplier;
    public List<String> unit;

    public ReadingTypePatternAttributeInfo() {
    }
}