/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormulaInfo {
    public String description;
    public List<ReadingTypeRequirementsInfo> readingTypeRequirements;
    public List<CustomPropertiesInfo> customProperties;

    public FormulaInfo() {
    }
}
