/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link com.energyict.mdc.scheduling.model.impl.HasValidTemporalExpression} constraint against a {@link TemporalExpression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-06 (08:52)
 */
public class HasTemporalExpressionValidator implements ConstraintValidator<HasValidTemporalExpression, NextExecutionSpecsImpl> {

    @Override
    public void initialize(HasValidTemporalExpression constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(NextExecutionSpecsImpl nextExecutionSpecs, ConstraintValidatorContext context) {
        TemporalExpression temporalExpression = nextExecutionSpecs.getTemporalExpression();
        if (temporalExpression == null) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY + "}")
                .addPropertyNode("temporalExpression").addConstraintViolation();
            return false;
        }
        else {
            return this.validateTemporalSpecs(temporalExpression, context);
        }
    }

    private enum TimeDurationAspect {
        FREQUENCY {
            @Override
            boolean validateCount(int timeDuractionCount, ConstraintValidatorContext context) {
                if (timeDuractionCount <= 0) {
                    context.disableDefaultConstraintViolation();
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY + "}")
                        .addPropertyNode("temporalExpression.every").addConstraintViolation();
                    return false;
                }
                else {
                    return true;
                }
            }
        },

        OFFSET {
            @Override
            boolean validateCount(int timeDuractionCount, ConstraintValidatorContext context) {
                if (timeDuractionCount < 0) {
                    context.disableDefaultConstraintViolation();
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY + "}")
                        .addPropertyNode("temporalExpression.offset").addConstraintViolation();
                    return false;
                }
                else {
                    return true;
                }
            }
        };

        abstract boolean validateCount(int timeDuractionCount, ConstraintValidatorContext context);
    }

    protected boolean validateTemporalSpecs (TemporalExpression temporalExpression, ConstraintValidatorContext context) {
        return this.validateFrequency(temporalExpression, context)
                && this.validateOffset(temporalExpression, context);
    }

    private boolean validateFrequency(TemporalExpression temporalExpression, ConstraintValidatorContext context) {
        if (temporalExpression.getEvery() == null) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY + "}")
                .addPropertyNode("temporalExpression.every").addConstraintViolation();
            return false;
        }
        else {
            return this.validateTimeDuration(temporalExpression.getEvery(), TimeDurationAspect.FREQUENCY, "every", context);
        }
    }

    private boolean validateOffset(TemporalExpression temporalExpression, ConstraintValidatorContext context) {
        if (temporalExpression.getOffset() != null) {
            return this.validateTimeDuration(temporalExpression.getOffset(), TimeDurationAspect.OFFSET, "offset", context);
        }
        else {
            return true;
        }
    }

    private boolean validateTimeDuration (TimeDuration timeDuration, TimeDurationAspect countValidator, String aspect, ConstraintValidatorContext context) {
        switch (timeDuration.getTimeUnit()) {
            case HOURS: // Intentional fall-through
            case MINUTES: // Intentional fall-through
            case SECONDS: // Intentional fall-through
            case MILLISECONDS: // Intentional fall-through
            case DAYS: // Intentional fall-through
            case WEEKS: // Intentional fall-through
            case MONTHS: // Intentional fall-through
            case YEARS: {
                // All is fine
                break;
            }
            default: {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY + "}")
                        .addPropertyNode("temporalExpression." + aspect).addConstraintViolation();
                return false;
            }
        }
        return countValidator.validateCount(timeDuration.getCount(), context);
    }
}