package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "CST";

    private BigDecimal constantValue;

    public ConstantNode() {
        super();
    }

    public ConstantNode(BigDecimal value) {
        this();
        this.constantValue = value;
    }

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

    public Dimension getDimension() {
       return Dimension.DIMENSIONLESS;
    }


}