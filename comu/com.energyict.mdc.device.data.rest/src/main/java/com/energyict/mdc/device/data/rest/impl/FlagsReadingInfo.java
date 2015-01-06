package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.Register;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class FlagsReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    public Long value;

    public FlagsReadingInfo() {}

    public FlagsReadingInfo(FlagsReading reading, NumericalRegisterSpec registerSpec) {
        super(reading);
        this.value = reading.getFlags();
    }

    @Override
    protected BaseReading createNew(Register register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), BigDecimal.valueOf(this.value), this.timeStamp);
    }

}