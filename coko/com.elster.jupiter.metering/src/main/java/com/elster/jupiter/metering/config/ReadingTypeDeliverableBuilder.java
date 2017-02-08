/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;

/**
 * Support the building process of a {@link ReadingTypeDeliverable}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-30 (11:47)
 */
@ProviderType
public interface ReadingTypeDeliverableBuilder {

    FormulaBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable);

    FormulaBuilder requirement(ReadingTypeRequirement value);

    FormulaBuilder property(CustomPropertySet customPropertySet, PropertySpec propertySpec);

    FormulaBuilder nullValue();

    FormulaBuilder constant(BigDecimal value);

    FormulaBuilder constant(long value);

    FormulaBuilder constant(double value);

    FormulaBuilder minimum(FormulaBuilder firstTerm, FormulaBuilder secondTerm, FormulaBuilder... terms);

    FormulaBuilder maximum(FormulaBuilder firstTerm, FormulaBuilder secondTerm, FormulaBuilder... terms);

    FormulaBuilder sum(AggregationLevel aggregationLevel, FormulaBuilder term);

    FormulaBuilder minimum(AggregationLevel aggregationLevel, FormulaBuilder term);

    FormulaBuilder maximum(AggregationLevel aggregationLevel, FormulaBuilder term);

    FormulaBuilder average(AggregationLevel aggregationLevel, FormulaBuilder term);

    FormulaBuilder aggregate(FormulaBuilder expression);

    FormulaBuilder power(FormulaBuilder expression, FormulaBuilder exponent);

    FormulaBuilder squareRoot(FormulaBuilder expression);

    FormulaBuilder plus(FormulaBuilder term1, FormulaBuilder term2);

    FormulaBuilder minus(FormulaBuilder term1, FormulaBuilder term2);

    FormulaBuilder multiply(FormulaBuilder multiplier, FormulaBuilder multiplicand);

    FormulaBuilder divide(FormulaBuilder dividend, FormulaBuilder divisor);

    FormulaBuilder safeDivide(FormulaBuilder dividend, FormulaBuilder divisor, FormulaBuilder zeroReplacement);

    FormulaBuilder firstNotNull(FormulaBuilder firstTerm, FormulaBuilder... remainingTerms);

    ReadingTypeDeliverable build(FormulaBuilder nodeBuilder);

}