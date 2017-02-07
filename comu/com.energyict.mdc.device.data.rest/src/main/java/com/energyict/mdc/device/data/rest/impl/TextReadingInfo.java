/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.device.data.Register;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    public String value;

    public TextReadingInfo() {}

    @Override
    protected BaseReading createNew(Register<?, ?> register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp);
    }

}