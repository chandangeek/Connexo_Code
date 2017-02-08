/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NumericalOutputRegisterDataInfo extends OutputRegisterDataInfo {

    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    @JsonProperty("calculatedValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal calculatedValue;

    public Boolean isConfirmed;

    @Override
    public BaseReading createNew(ReadingType readingType) {
        return ReadingImpl.of(readingType.getMRID(), this.value, this.timeStamp);
    }
}
