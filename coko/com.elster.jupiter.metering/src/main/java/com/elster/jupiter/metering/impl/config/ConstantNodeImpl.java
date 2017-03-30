/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNodeImpl extends AbstractNode implements ConstantNode {

    static final String TYPE_IDENTIFIER = "CST";

    private BigDecimal constantValue;

    public ConstantNodeImpl() {
        super();
    }

    public ConstantNodeImpl(BigDecimal value) {
        this();
        this.constantValue = value;
    }

    @Override
    public BigDecimal getValue() {
        return constantValue;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitConstant(this);
    }

    public String toString() {
        return "constant(" + constantValue.toString() + ")";
    }

    @Override
    public Dimension getDimension() {
        return Dimension.DIMENSIONLESS;
    }

    @Override
    public void validate() {
        // No validation for constants
    }

}