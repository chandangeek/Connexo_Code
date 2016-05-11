package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models a {@link ServerExpressionNode} that holds a String constant.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-19 (09:35)
 */
class StringConstantNode implements ServerExpressionNode {

    private final String value;

    StringConstantNode(String value) {
        super();
        this.value = value;
    }

    String getValue() {
        return this.value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitConstant(this);
    }

}