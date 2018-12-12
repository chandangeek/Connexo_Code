/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Assert a usage point has only a unique detail for a certain interval(start)
 */
public class HasUniqueDetailsValidator implements ConstraintValidator<HasUniqueDetailsForInterval, UsagePointDetailImpl> {

    private final MeteringService meteringService;
    private String message;

    @Inject
    public HasUniqueDetailsValidator(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void initialize(HasUniqueDetailsForInterval hasUniqueDetailsForInterval) {
        message = hasUniqueDetailsForInterval.message();
    }

    @Override
    public boolean isValid(UsagePointDetailImpl usagePointDetail, ConstraintValidatorContext constraintValidatorContext) {
        if (usagePointDetail.getUsagePoint().getDetails().stream().filter(detail -> detail.getRange().contains(usagePointDetail.getRange().lowerEndpoint())).findFirst().isPresent()) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("interval").addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        };
        return true;
    }
}
