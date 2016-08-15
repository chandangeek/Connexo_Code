package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.mdm.usagepoint.config.rest.impl.FormulaInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingTypeDeliverablesInfo {
    public long id;
    public String name;
    public ReadingTypeInfo readingType;
    public FormulaInfo formula;
}
