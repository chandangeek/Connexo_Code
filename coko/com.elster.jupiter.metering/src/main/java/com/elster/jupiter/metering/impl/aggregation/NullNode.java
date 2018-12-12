/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;


import com.elster.jupiter.util.units.Dimension;

/**
 * Models a {@link ServerExpressionNode} that represents the <code>null</code> literal.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-19 (09:35)
 */
class NullNode implements ServerExpressionNode {

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNull(this);
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(Dimension.DIMENSIONLESS);
    }

}