/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.rest.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    public String value;
    @JsonProperty("interval")
    public IntervalInfo interval;

    public TextReadingInfo() {}

    @Override
    protected BaseReading createNew(Register<?, ?> register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp);
    }

}