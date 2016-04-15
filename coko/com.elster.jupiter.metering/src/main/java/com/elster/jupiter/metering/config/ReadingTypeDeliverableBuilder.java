package com.elster.jupiter.metering.config;

import java.math.BigDecimal;

/**
 * Support the building process of a {@link ReadingTypeDeliverable}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-30 (11:47)
 */
public interface ReadingTypeDeliverableBuilder {

    FormulaBuilder deliverable(ReadingTypeDeliverable readingTypeDeliverable);

    FormulaBuilder requirement(ReadingTypeRequirement value);

    FormulaBuilder requirement(ReadingTypeRequirementNode existingNode);

    FormulaBuilder nullValue();

    FormulaBuilder constant(BigDecimal value);

    FormulaBuilder constant(long value);

    FormulaBuilder constant(double value);

    FormulaBuilder sum(FormulaBuilder... terms);

    FormulaBuilder maximum(FormulaBuilder... terms);

    FormulaBuilder minimum(FormulaBuilder... terms);

    FormulaBuilder average(FormulaBuilder... terms);

    FormulaBuilder aggregate(FormulaBuilder expression);

    FormulaBuilder plus(FormulaBuilder term1, FormulaBuilder term2);

    FormulaBuilder minus(FormulaBuilder term1, FormulaBuilder term2);

    FormulaBuilder divide(FormulaBuilder dividend, FormulaBuilder divisor);

    FormulaBuilder safeDivide(FormulaBuilder dividend, FormulaBuilder divisor, FormulaBuilder zeroReplacement);

    FormulaBuilder multiply(FormulaBuilder multiplier, FormulaBuilder multiplicand);

    ReadingTypeDeliverable build(FormulaBuilder nodeBuilder);

}