package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class DeliverableValidator implements ConstraintValidator<ValidDeliverable, ReadingTypeDeliverable> {

    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public DeliverableValidator(ServerMetrologyConfigurationService metrologyConfigurationService) {
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
            if ((readingType != null) && (!readingType.isRegular())) {
                throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.IRREGULAR_READINGTYPE_IN_DELIVERABLE);
            }
            Formula formula = deliverable.getFormula();
            if (readingType != null && formula.getMode().equals(Formula.Mode.AUTO) &&  !UnitConversionSupport.isAssignable(readingType, formula.getExpressionNode().getDimension())) {
                throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA);
            }
            if (readingType != null) {
                IntervalLength intervalLengthOfReadingType = IntervalLength.from(readingType);
                IntervalLength intervalLengthOfFormula = ((ServerFormula) formula).getIntervalLength();
                //if no wildcards on interval in the requirements of the fomula
                if (!intervalLengthOfFormula.equals(IntervalLength.NOT_SUPPORTED)) {
                    if (intervalLengthOfReadingType.ordinal() < intervalLengthOfFormula.ordinal()) {
                        throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS);
                    }
                }
            }
            /*for (ReadingTypeDeliverable del : deliverable.getMetrologyConfiguration().getDeliverables()) {
                if (!del.equals(this)) {
                    if (formula.getMode().equals(Formula.Mode.AUTO) &&
                            !UnitConversionSupport.isAssignable(del.getReadingType(), formula.getExpressionNode().getDimension())) {
                        throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA);
                    }
                }
            }*/
            return true;
        } catch (InvalidNodeException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }
}
