/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;

/**
 * Models a {@link ServerExpressionNode} that holds a numerical constant.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-19 (09:35)
 */
class NumericalConstantNode implements ServerExpressionNode {

    private final BigDecimal value;

    NumericalConstantNode(BigDecimal value) {
        super();
        this.value = value;
    }

    BigDecimal getValue() {
        return this.value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitConstant(this);
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(Dimension.DIMENSIONLESS);
    }

}