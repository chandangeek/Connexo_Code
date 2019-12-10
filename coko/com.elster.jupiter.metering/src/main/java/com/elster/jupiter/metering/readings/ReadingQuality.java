/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.ReadingQualityType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface ReadingQuality {
    @XmlAttribute
    String getComment();

    // returns systemId.category.subCategory identifier of the readingQuality's type
    @XmlAttribute
    String getTypeCode();

    @XmlElement
    default ReadingQualityType getType() {
        return new ReadingQualityType(getTypeCode());
    }
}
