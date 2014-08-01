package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.TextReading;
import org.codehaus.jackson.annotate.JsonProperty;

public class TextReadingInfo extends ReadingInfo<TextReading, TextualRegisterSpec> {
    @JsonProperty("value")
    public String value;

    public TextReadingInfo() {}

    public TextReadingInfo(TextReading reading, TextualRegisterSpec registerSpec) {
        super(reading, registerSpec);
        this.value = reading.getValue();
    }
}
