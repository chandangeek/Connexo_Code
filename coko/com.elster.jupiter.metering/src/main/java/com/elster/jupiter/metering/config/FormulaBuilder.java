package com.elster.jupiter.metering.config;

import java.math.BigDecimal;

/**
 * Created by igh on 26/02/2016.
 */
public interface FormulaBuilder {

    ExpressionNodeBuilder constant(BigDecimal value);
    ExpressionNodeBuilder constant(long value);
    ExpressionNodeBuilder constant(double value);
    ExpressionNodeBuilder sum(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder maximum(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder minimum(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder average(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2);
    ExpressionNodeBuilder minus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2);
    ExpressionNodeBuilder divide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor);
    ExpressionNodeBuilder multiply(ExpressionNodeBuilder multiplier, ExpressionNodeBuilder multiplicand);
    ExpressionNodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable);
    ExpressionNodeBuilder requirement(ReadingTypeRequirement value);
    FormulaBuilder init(ExpressionNodeBuilder nodeBuilder);
    FormulaBuilder init(ExpressionNode expressionNode);
    Formula build();
}
