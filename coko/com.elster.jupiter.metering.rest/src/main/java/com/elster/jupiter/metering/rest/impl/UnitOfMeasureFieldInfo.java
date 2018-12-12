/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UnitOfMeasureFieldInfo {
    public String name;
    public long multiplier;
    public int unit;

    public UnitOfMeasureFieldInfo(MetricMultiplier multiplier, ReadingTypeUnit unit) {
        this.name=multiplier.getSymbol() + unit.getSymbol();
        this.multiplier = Long.valueOf(multiplier.getMultiplier()).byteValue() & 0xFF;
        this.unit=unit.getId();
    }
}