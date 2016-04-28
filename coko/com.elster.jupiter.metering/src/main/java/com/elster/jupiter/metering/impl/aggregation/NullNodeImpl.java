package com.elster.jupiter.metering.impl.aggregation;


/**
 * Models a {@link ServerExpressionNode} that represents the <code>null</code> leteral.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-19 (09:35)
 */
class NullNodeImpl implements ServerExpressionNode {

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNull(this);
    }

}