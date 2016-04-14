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
import java.util.List;


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
            Formula formula = deliverable.getFormula();
            ReadingType readingType = deliverable.getReadingType();
            if ((readingType != null) && formula.getMode().equals(Formula.Mode.AUTO)) {
                if (!readingType.isRegular()) {
                    throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.IRREGULAR_READINGTYPE_IN_DELIVERABLE);
                }
                if (!UnitConversionSupport.isValidForAggregation(readingType)) {
                    throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.INVALID_READINGTYPE_IN_DELIVERABLE);
                }
            }
            if ((readingType != null) && formula.getMode().equals(Formula.Mode.AUTO) &&  !UnitConversionSupport.isAssignable(readingType, formula.getExpressionNode().getDimension())) {
                throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA,
                        readingType.getMRID() + " (" + readingType.getFullAliasName() + ")", deliverable.getFormula().getExpressionNode().toString(),  deliverable.getName());
            }
            if (readingType != null) {
                IntervalLength intervalLengthOfReadingType = IntervalLength.from(readingType);
                IntervalLength intervalLengthOfFormula = ((ServerFormula) formula).getIntervalLength();
                //if no wildcards on interval in the requirements of the formula
                if (!intervalLengthOfFormula.equals(IntervalLength.NOT_SUPPORTED)) {
                    if (intervalLengthOfReadingType.ordinal() < intervalLengthOfFormula.ordinal()) {
                        throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS);
                    }
                    List<IntervalLength> lengths = ((ServerFormula) formula).getIntervalLengths();
                    for (IntervalLength length : lengths) {
                        if (!UnitConversionSupport.isAssignable(intervalLengthOfReadingType, length)) {
                            throw new InvalidNodeException(
                                    metrologyConfigurationService.getThesaurus(),
                                    MessageSeeds.INCOMPATIBLE_INTERVAL_LENGTHS, length.toString(), intervalLengthOfReadingType.toString());
                        }
                    }
                }
            }
            for (ReadingTypeDeliverable del : deliverable.getMetrologyConfiguration().getDeliverables()) {
                if (!del.equals(deliverable)) {
                    if (formula.getMode().equals(Formula.Mode.AUTO) &&
                            !UnitConversionSupport.isAssignable(del.getReadingType(), del.getFormula().getExpressionNode().getDimension())) {
                        throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(),
                                MessageSeeds.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA,
                                del.getReadingType().getMRID() + " (" + del.getReadingType().getFullAliasName() + ")",
                                del.getFormula().getExpressionNode().toString(),
                                del.getName());
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
}
