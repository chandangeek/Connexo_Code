package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models a {@link ServerExpressionNode} that references a variable.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-04 (13:29)
 */
class VariableReferenceNode implements ServerExpressionNode {

    private final String name;

    VariableReferenceNode(String name) {
        super();
        this.name = name;
    }

    /**
     * Gets the name of the variable referenced by this node.
     *
     * @return The name of the variable
     */
    String getName() {
        return this.name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVariable(this);
    }

}