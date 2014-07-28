package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.TextReading;
import org.codehaus.jackson.annotate.JsonProperty;

public class TextReadingInfo extends ReadingInfo<TextReading> {
    @JsonProperty("value")
    public String value;

    public TextReadingInfo() {}

    public TextReadingInfo(TextReading reading, Register register) {
        super(reading, register);
        this.value = reading.getValue();
    }
}
