package com.elster.jupiter.metering.impl.config;

import javax.inject.Inject;
import java.math.BigDecimal;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "CST";

    private BigDecimal constantValue;

    @Inject
    public ConstantNode() {}

    public ConstantNode(BigDecimal value) {
        super();
        this.constantValue = value;
    }

    public BigDecimal getValue() {
        return constantValue;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitConstant(this);
    }

}