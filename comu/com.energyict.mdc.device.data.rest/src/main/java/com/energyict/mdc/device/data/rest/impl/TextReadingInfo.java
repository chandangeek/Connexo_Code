package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.TextReading;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import org.codehaus.jackson.annotate.JsonProperty;

public class TextReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    public String value;

    public TextReadingInfo() {}

    public TextReadingInfo(TextReading reading, TextualRegisterSpec registerSpec) {
        super(reading);
        this.value = reading.getValue();
    }

    @Override
    protected BaseReading createNew(Register register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp.toInstant());
    }

}