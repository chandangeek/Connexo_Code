package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Unit;
import com.energyict.mdc.device.data.EventReading;
import com.energyict.mdc.device.data.Register;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

public class EventReadingInfo extends ReadingInfo<EventReading> {
    @JsonProperty("value")
    public BigDecimal value;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(ReadingUnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("interval")
    public IntervalInfo interval;

    public EventReadingInfo() {}

    public EventReadingInfo(EventReading reading, Register register) {
        super(reading, register);
        this.value = reading.getQuantity().getValue();
        this.unitOfMeasure = reading.getQuantity().getUnit();
        if(reading.getInterval().isPresent()) {
            this.interval = new IntervalInfo(reading.getInterval().get());
        }
    }

    public static class IntervalInfo {
        @JsonProperty("start")
        public Long start;
        @JsonProperty("end")
        public Long end;

        public IntervalInfo() {}

        public IntervalInfo(Interval interval) {
            this.start = interval.getStart().getTime();
            this.end = interval.getEnd().getTime();
        }
    }
}
