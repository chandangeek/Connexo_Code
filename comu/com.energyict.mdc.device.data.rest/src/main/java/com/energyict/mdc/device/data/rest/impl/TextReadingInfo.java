package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.TextReading;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

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
        BaseReading reading = new ReadingImpl(register.getReadingType().getMRID(), this.value, this.id == null ? this.timeStamp : new Date(this.id));
        return reading;
    }
}
