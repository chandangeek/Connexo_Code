package com.energyict.mdc.device.data.rest.impl;


import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

public class NumericalReadingInfo extends ReadingInfo<NumericalReading, NumericalRegisterSpec> {
    @JsonProperty("value")
    public BigDecimal value;
    @JsonProperty("rawValue")
    public BigDecimal rawValue;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;

    public NumericalReadingInfo() {}

    public NumericalReadingInfo(NumericalReading reading, NumericalRegisterSpec registerSpec) {
        super(reading, registerSpec);
        this.value = reading.getQuantity().getValue();
        this.rawValue = reading.getQuantity().getValue();
        this.unitOfMeasure = registerSpec.getUnit();
        this.multiplier = registerSpec.getMultiplier();
    }
}
