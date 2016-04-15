package com.elster.jupiter.metering.impl.aggregation;


/**
 * Models a {@link ServerExpressionNode} that holds a numerical constant.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-19 (09:35)
 */
class NullNode implements ServerExpressionNode {

    @Override
    public <T> T accept(Visitor<T> visitor) {
        //return visitor.visitNull(this);
        //todo
        return null;
    }

}
