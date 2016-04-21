package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingTypeRequirementsInfo {
    public String type;
    public IdWithNameInfo meterRole;
    public ReadingTypeInfo readingType;

    public ReadingTypeRequirementsInfo() {
    }
}
