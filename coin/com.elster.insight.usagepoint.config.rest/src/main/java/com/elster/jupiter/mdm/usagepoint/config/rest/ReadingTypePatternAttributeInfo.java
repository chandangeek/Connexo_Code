/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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