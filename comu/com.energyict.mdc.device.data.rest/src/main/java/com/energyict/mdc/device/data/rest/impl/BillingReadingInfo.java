package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;

import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Unit;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

public class BillingReadingInfo extends ReadingInfo<BillingReading, NumericalRegisterSpec> {
    @JsonProperty("value")
    public BigDecimal value;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(ReadingUnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;

    public BillingReadingInfo() {
    }

    public BillingReadingInfo(BillingReading reading, NumericalRegisterSpec registerSpec) {
        super(reading, registerSpec);
        this.value = reading.getQuantity().getValue();
        this.unitOfMeasure = reading.getQuantity().getUnit();
        if (reading.getInterval().isPresent()) {
            this.interval = new IntervalInfo(reading.getInterval().get());
        }
    }

    public static class IntervalInfo {
        @JsonProperty("start")
        public Long start;
        @JsonProperty("end")
        public Long end;

        public IntervalInfo() {
        }

        public IntervalInfo(Interval interval) {
            this.start = interval.getStart().getTime();
            this.end = interval.getEnd().getTime();
        }
    }

}