package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UnitOfMeasureFieldInfo {
    public String name;
    public int multiplier;
    public int unit;

    public UnitOfMeasureFieldInfo(MetricMultiplier multiplier, ReadingTypeUnit unit) {
        this.name=multiplier.getSymbol() + unit.getSymbol();
        this.multiplier=multiplier.getMultiplier();
        this.unit=unit.getId();
    }
}