/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.properties.PropertySpec;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-21 (15:18)
 */
public interface ServerFormulaBuilder extends FormulaBuilder {

    ExpressionNodeBuilder nullValue();

    ExpressionNodeBuilder constant(BigDecimal value);

    ExpressionNodeBuilder constant(long value);

    ExpressionNodeBuilder constant(double value);

    ExpressionNodeBuilder minimum(List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder maximum(List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder sum(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder minimum(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder maximum(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder average(AggregationLevel aggregationLevel, List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder firstNotNull(List<ExpressionNodeBuilder> terms);

    ExpressionNodeBuilder aggregate(ExpressionNodeBuilder expression);

    ExpressionNodeBuilder plus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2);

    ExpressionNodeBuilder minus(ExpressionNodeBuilder term1, ExpressionNodeBuilder term2);

    ExpressionNodeBuilder divide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor);

    ExpressionNodeBuilder safeDivide(ExpressionNodeBuilder dividend, ExpressionNodeBuilder divisor, ExpressionNodeBuilder zeroReplacementNode);

    ExpressionNodeBuilder multiply(ExpressionNodeBuilder multiplier, ExpressionNodeBuilder multiplicand);

    ExpressionNodeBuilder power(ExpressionNodeBuilder term, ExpressionNodeBuilder exponent);

    ExpressionNodeBuilder squareRoot(ExpressionNodeBuilder term);

    ExpressionNodeBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable);

    ExpressionNodeBuilder requirement(ReadingTypeRequirement value);

    ExpressionNodeBuilder property(RegisteredCustomPropertySet customPropertySet, PropertySpec propertySpec);

    Formula build();

    ServerFormulaBuilder init(ExpressionNodeBuilder nodeBuilder);

    ServerFormulaBuilder init(ServerExpressionNode expressionNode);

}