package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.units.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import java.math.BigDecimal;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

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
            this.interval = IntervalInfo.from(reading.getInterval().get());
        }
    }

}