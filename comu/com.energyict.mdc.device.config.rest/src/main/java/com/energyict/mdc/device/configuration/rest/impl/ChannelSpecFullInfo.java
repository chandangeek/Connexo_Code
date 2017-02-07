/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelSpecFullInfo extends ChannelSpecInfo {
    @JsonProperty("measurementType")
    public ChannelSpecShortInfo measurementType;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public BigDecimal overflowValue;
    public int nbrOfFractionDigits;
    public Boolean isLinkedByActiveDeviceConfiguration;
    @JsonProperty("useMultiplier")
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;           // the calculated readingType in case of a 'bulk' collectedReadingType
    @JsonProperty("multipliedCalculatedReadingType")
    public ReadingTypeInfo multipliedCalculatedReadingType; // the calculated readingType which is applicable after multiplying
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    @JsonProperty("collectedReadingType")
    public ReadingTypeInfo collectedReadingType;
    public long version;
    public VersionInfo<Long> parent;
}
