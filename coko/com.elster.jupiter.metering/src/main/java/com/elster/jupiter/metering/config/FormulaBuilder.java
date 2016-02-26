package com.elster.jupiter.metering.config;

import java.math.BigDecimal;

/**
 * Created by igh on 26/02/2016.
 */
public interface FormulaBuilder {

    NodeBuilder constant(BigDecimal value);
    NodeBuilder constant(long value);
    NodeBuilder constant(double value);
    NodeBuilder sum(NodeBuilder... terms);
    NodeBuilder maximum(NodeBuilder... terms);
    NodeBuilder minimum(NodeBuilder... terms);
    NodeBuilder average(NodeBuilder... terms);
    NodeBuilder plus(NodeBuilder term1, NodeBuilder term2);
    NodeBuilder minus(NodeBuilder term1, NodeBuilder term2);
    NodeBuilder divide(NodeBuilder dividend, NodeBuilder divisor);
    NodeBuilder multiply(NodeBuilder multiplier, NodeBuilder multiplicand);
}
