package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.FlagsReading;
import org.codehaus.jackson.annotate.JsonProperty;

public class FlagsReadingInfo extends ReadingInfo<FlagsReading, NumericalRegisterSpec> {
    @JsonProperty("value")
    public Long value;

    public FlagsReadingInfo() {}

    public FlagsReadingInfo(FlagsReading reading, NumericalRegisterSpec registerSpec) {
        super(reading, registerSpec);
        this.value = reading.getFlags();
    }
}
