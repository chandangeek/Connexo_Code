package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadingTypeDeliverablesInfo {
    public long id;
    public String name;
    public ReadingTypeInfo readingType;

    public static ReadingTypeDeliverablesInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        ReadingTypeDeliverablesInfo info = new ReadingTypeDeliverablesInfo();
        info.id = readingTypeDeliverable.getId();
        info.name = readingTypeDeliverable.getName();
        info.readingType = readingTypeDeliverable.getReadingType() != null ? new ReadingTypeInfo(readingTypeDeliverable.getReadingType()) : null;
        return info;
    }
}
