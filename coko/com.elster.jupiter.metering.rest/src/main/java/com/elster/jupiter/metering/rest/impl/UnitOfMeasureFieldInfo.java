package com.elster.jupiter.metering.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UnitOfMeasureFieldInfo {
    public String name;
    public int multiplier;
    public int unit;

    public UnitOfMeasureFieldInfo(String name, int multiplier, int unit) {
        this.name = name;
        this.multiplier = multiplier;
        this.unit = unit;
    }
}