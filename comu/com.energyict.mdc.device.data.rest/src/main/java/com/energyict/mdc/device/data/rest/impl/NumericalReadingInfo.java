package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Register;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

public class NumericalReadingInfo extends ReadingInfo<NumericalReading> {
    @JsonProperty("value")
    public BigDecimal value;
    @JsonProperty("rawValue")
    public BigDecimal rawValue;

    public NumericalReadingInfo() {}

    public NumericalReadingInfo(NumericalReading reading, Register register) {
        super(reading, register);
        this.value = reading.getQuantity().getValue();
        this.rawValue = reading.getQuantity().getValue();
    }
}
