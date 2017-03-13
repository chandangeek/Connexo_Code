/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.cbo.Unit;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author koen
 */
public class LoadSurveyChannel implements Serializable {

    private int register;
    private int width;
    private int type; // internal data type
    private Unit unit;
    private String name;
    private int offset;
    private BigDecimal scalingFactor;

    public LoadSurveyChannel() {
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public BigDecimal getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(BigDecimal scalingFactor) {
        this.scalingFactor = scalingFactor;
    }
}