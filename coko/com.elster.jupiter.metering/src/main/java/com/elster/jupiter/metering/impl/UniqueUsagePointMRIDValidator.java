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

public class UniqueUsagePointMRIDValidator implements ConstraintValidator<UniqueMRID, UsagePoint> {

    private String message;
    private MeteringService meteringService;

    @Inject
    public UniqueUsagePointMRIDValidator(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void initialize(UniqueMRID constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UsagePoint usagePoint, ConstraintValidatorContext context) {
        return Optional.ofNullable(usagePoint)
                .map(UsagePoint::getMRID)
                .flatMap(meteringService::findUsagePointByMRID)
                .filter(alreadyPresentUP -> usagePoint.getId() != alreadyPresentUP.getId())
                .map(conflictingUP -> fail(context))
                .orElse(true);
    }

    private boolean fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("mRID")
                .addConstraintViolation();
        return false; // something is not valid
    }
}
