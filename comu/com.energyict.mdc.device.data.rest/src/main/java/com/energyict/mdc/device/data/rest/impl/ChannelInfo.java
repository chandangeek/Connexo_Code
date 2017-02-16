/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * JSON representation of a channel
 * Created by bvn on 8/6/14.
 */
public class ChannelInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public Instant lastReading;
    public Instant lastValueTimestamp;
    public ReadingTypeInfo readingType;
    public ReadingTypeInfo calculatedReadingType;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public BigDecimal overflowValue;
    public BigDecimal overruledOverflowValue;
    public String flowUnit;
    public Integer nbrOfFractionDigits;
    public Integer overruledNbrOfFractionDigits;
    public long loadProfileId;
    public String loadProfileName;
    public long version;
    public VersionInfo<String> parent;
    public Boolean useMultiplier;
    public BigDecimal multiplier;
    public String dataloggerSlaveName;

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;
}
