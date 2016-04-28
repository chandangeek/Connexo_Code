package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.util.units.Dimension;

/**
 * Created by igh on 15/04/2016.
 */
public class NullNodeImpl extends AbstractNode implements NullNode {

    static final String TYPE_IDENTIFIER = "NUL";

    public NullNodeImpl() {
        super();
    }

    @Override
    public <T> T accept(ExpressionNode.Visitor<T> visitor) {
        return visitor.visitNull(this);
    }

    public String toString() {
        return "null";
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