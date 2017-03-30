/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AllRequiredCustomPropertySetsHaveValuesValidator implements ConstraintValidator<AllRequiredCustomPropertySetsHaveValues, UsagePointImpl> {

    @Override
    public void initialize(AllRequiredCustomPropertySetsHaveValues constraintAnnotation) {
        // nothing to do
    }

    @Override
    public boolean isValid(UsagePointImpl usagePoint, ConstraintValidatorContext context) {
        if (usagePoint.forCustomProperties().getAllPropertySets().stream()
                .filter(propertySet -> propertySet.getCustomPropertySet().isRequired())
                .anyMatch(propertySet -> propertySet.getValues() == null || propertySet.getValues().isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.REQUIRED_CAS_MISSING + "}")
                    .addPropertyNode("customPropertySets")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
