/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueUsagePointNameValidator implements ConstraintValidator<UniqueName, UsagePoint> {

    private String message;
    private MeteringService meteringService;

    @Inject
    public UniqueUsagePointNameValidator(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UsagePoint usagePoint, ConstraintValidatorContext context) {
        return Optional.ofNullable(usagePoint)
                .map(UsagePoint::getName)
                .flatMap(meteringService::findUsagePointByName)
                .filter(alreadyPresentUP -> usagePoint.getId() != alreadyPresentUP.getId())
                .map(conflictingUP -> fail(context))
                .orElse(true);
    }

    private boolean fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("name")
                .addConstraintViolation();
        return false; // something is not valid
    }
}
