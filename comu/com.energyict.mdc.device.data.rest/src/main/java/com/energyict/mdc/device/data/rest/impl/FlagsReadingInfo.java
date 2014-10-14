package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.Register;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

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
        BaseReading reading = new ReadingImpl(register.getReadingType().getMRID(), BigDecimal.valueOf(this.value), this.timeStamp);
        return reading;
    }
}
