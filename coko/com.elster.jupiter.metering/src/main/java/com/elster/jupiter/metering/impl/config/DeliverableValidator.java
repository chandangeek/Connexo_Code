/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.cbo.Accumulation.BULKQUANTITY;

class DeliverableValidator implements ConstraintValidator<ValidDeliverable, ReadingTypeDeliverable> {

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    DeliverableValidator(ServerMetrologyConfigurationService metrologyConfigurationService) {
        super();
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public void initialize(ValidDeliverable constraintAnnotation) {
    }

    @Override
    public boolean isValid(ReadingTypeDeliverable deliverable, ConstraintValidatorContext context) {
        try {
            ReadingType readingType = deliverable.getReadingType();
            if ((readingType != null)) {
                if (!this.isValid(deliverable, readingType, context)) {
                    return false;
                }
            }
            Formula formula = deliverable.getFormula();
            for (ReadingTypeDeliverable del : deliverable.getMetrologyConfiguration().getDeliverables()) {
                if (!del.equals(deliverable)) {
                    if (formula.getMode().equals(Formula.Mode.AUTO) &&
                            !UnitConversionSupport.isAssignable(del.getReadingType(), del.getFormula().getExpressionNode().getDimension())) {
                        throw InvalidNodeException.deliverableReadingTypeIsNotCompatibleWithFormula(metrologyConfigurationService.getThesaurus(), del.getReadingType(), del);
                    }
                }
            }
            this.validateConsistentAggregationLevels(formula.getExpressionNode());
            return true;
        } catch (InvalidNodeException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

    private boolean isValid(ReadingTypeDeliverable deliverable, ReadingType readingType, ConstraintValidatorContext context) {
        try {
            Formula formula = deliverable.getFormula();
            if (readingType.isRegular()) {
                if (!formula.getExpressionNode().accept(new RegularDeliverableComplexityAnalyzer())) {
                    throw new InvalidNodeException(this.metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.REGULAR_READING_TYPE_DELIVERABLE_DOES_NOT_SUPPORT_IRREGULAR_REQUIREMENTS);
                }
                if (this.isBulk(readingType)) {
                    if (!formula.getExpressionNode().accept(new OnlyBulkReadingTypeRequirements())) {
                        throw new InvalidNodeException(this.metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.BULK_DELIVERABLES_CAN_ONLY_USE_BULK_READINGTYPES);
                    }
                } else {
                    if (formula.getExpressionNode().accept(new NoBulkReadingTypeRequirements())) {
                        throw new InvalidNodeException(this.metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.BULK_READINGTYPE_NOT_ALLOWED);
                    }
                }
            } else {
                IrregularDeliverableComplexityAnalyzer complexity = new IrregularDeliverableComplexityAnalyzer();
                formula.getExpressionNode().accept(complexity);
                if (!complexity.isSimple()) {
                    throw new InvalidNodeException(this.metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.IRREGULAR_READING_TYPE_DELIVERABLE_ONLY_SUPPORTS_SIMPLE_FORMULAS);
                }
            }
            if (formula.getMode().equals(Formula.Mode.AUTO)) {
                if (!UnitConversionSupport.isValidForAggregation(readingType)) {
                    throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.INVALID_READINGTYPE_UNIT_IN_DELIVERABLE);
                }
                if (!UnitConversionSupport.isAssignable(readingType, formula.getExpressionNode().getDimension())) {
                    throw InvalidNodeException.deliverableReadingTypeIsNotCompatibleWithFormula(metrologyConfigurationService.getThesaurus(), readingType, deliverable);
                }
            }
            IntervalLength intervalLengthOfReadingType = IntervalLength.from(readingType);
            IntervalLength intervalLengthOfFormula = ((ServerFormula) formula).getIntervalLength();
            // if no wildcards on interval in the requirements of the formula
            if (!intervalLengthOfFormula.equals(IntervalLength.NOT_SUPPORTED)) {
                if (intervalLengthOfReadingType.ordinal() < intervalLengthOfFormula.ordinal()) {
                    throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS);
                }
                List<IntervalLength> lengths = ((ServerFormula) formula).getIntervalLengths();
                for (IntervalLength length : lengths) {
                    if (!UnitConversionSupport.isAssignable(intervalLengthOfReadingType, length)) {
                        throw InvalidNodeException.incompatibleIntervalLengths(
                                metrologyConfigurationService.getThesaurus(),
                                length,
                                intervalLengthOfReadingType);
                    }
                }
            }
            return true;
        } catch (InvalidNodeException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

    private void validateConsistentAggregationLevels(ExpressionNode expressionNode) {
        if (!expressionNode.accept(new ConsistentAggregationLevelValidator())) {
            throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), PrivateMessageSeeds.INCONSISTENT_LEVELS_IN_AGGREGATION_FUNCTIONS);
        }
    }

    private boolean isBulk(ReadingType readingType) {
        return BULKQUANTITY.equals(readingType.getAccumulation());
    }

    private class ConsistentAggregationLevelValidator implements ExpressionNode.Visitor<Boolean> {
        private AggregationLevel aggregationLevel = null;

        @Override
        public Boolean visitConstant(ConstantNode constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitProperty(CustomPropertyNode property) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitRequirement(ReadingTypeRequirementNode requirement) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitOperation(OperationNode operationNode) {
            return this.visitChildren(operationNode.getChildren());
        }

        @Override
        public Boolean visitFunctionCall(FunctionCallNode functionCall) {
            Optional<AggregationLevel> aggregationLevel = functionCall.getAggregationLevel();
            if (aggregationLevel.isPresent()) {
                if (this.aggregationLevel == null) {
                    this.aggregationLevel = aggregationLevel.get();
                    return this.visitChildren(functionCall.getChildren());
                } else {
                    if (this.aggregationLevel.equals(aggregationLevel.get())) {
                        return this.visitChildren(functionCall.getChildren());
                    } else {
                        return Boolean.FALSE;
                    }
                }
            } else {
                return Boolean.TRUE;
            }
        }

        private Boolean visitChildren(List<? extends ExpressionNode> children) {
            return children
                    .stream()
                    .map(each -> each.accept(this))
                    .reduce(null, this::and);
        }

        private Boolean and(Boolean b1, Boolean b2) {
            if (b1 == null) {
                return b2;
            } else if (b2 == null) {
                return b1;
            } else {
                return b1.booleanValue() && b2.booleanValue();
            }
        }

        @Override
        public Boolean visitNull(NullNode nullNode) {
            return Boolean.TRUE;
        }
    }

}