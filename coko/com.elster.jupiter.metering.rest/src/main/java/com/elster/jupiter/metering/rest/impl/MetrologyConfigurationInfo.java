/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyConfigurationInfo {

    public long id;
    public String name;
    public String description;
    public IdWithNameInfo status;
    public IdWithNameInfo serviceCategory;
    public List<ReadingTypeInfo> readingTypes;
    public long version;
}