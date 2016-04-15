package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import java.math.BigDecimal;

/**
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-21 (15:18)
 */
public interface ServerFormulaBuilder extends FormulaBuilder {

    ExpressionNodeBuilder nullValue();
    ExpressionNodeBuilder constant(BigDecimal value);
    ExpressionNodeBuilder constant(long value);
    ExpressionNodeBuilder constant(double value);
    ExpressionNodeBuilder sum(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder maximum(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder minimum(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder average(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder firstNotNull(ExpressionNodeBuilder... terms);
    ExpressionNodeBuilder aggregate(ExpressionNodeBuilder expression);
    ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2);
    ExpressionNodeBuilder minus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2);
    ExpressionNodeBuilder divide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor);
    ExpressionNodeBuilder safeDivide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor, ExpressionNodeBuilder zeroReplacementNode);
    ExpressionNodeBuilder multiply(ExpressionNodeBuilder multiplier, ExpressionNodeBuilder multiplicand);
    ExpressionNodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable);
    ExpressionNodeBuilder requirement(ReadingTypeRequirement value);
    Formula build();

    ServerFormulaBuilder init(ExpressionNodeBuilder nodeBuilder);

    ServerFormulaBuilder init(ExpressionNode expressionNode);

}