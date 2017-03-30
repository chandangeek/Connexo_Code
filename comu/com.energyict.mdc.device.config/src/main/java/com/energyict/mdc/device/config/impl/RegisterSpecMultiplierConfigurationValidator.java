/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.RegisterSpec;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Validates that you have a calculated readingtype when a multiplier is configured
 */
public class RegisterSpecMultiplierConfigurationValidator extends AbstractMultiplierConfigurationValidator implements ConstraintValidator<ValidRegisterSpecMultiplierConfiguration, NumericalRegisterSpecImpl> {
    @Override
    public void initialize(ValidRegisterSpecMultiplierConfiguration validRegisterSpecMultiplierConfiguration) {

    }

    @Override
    public boolean isValid(NumericalRegisterSpecImpl numericalRegisterSpec, ConstraintValidatorContext constraintValidatorContext) {
        if (numericalRegisterSpec.isUseMultiplier()) {
            ReadingType readingType = numericalRegisterSpec.getReadingType();
            if (readingTypeCanNotBeMultiplied(constraintValidatorContext, readingType)) {
                return false;
            }
            if (calculatedReadingTypeIsNotPresent(constraintValidatorContext, numericalRegisterSpec.getCalculatedReadingType())) {
                return false;
            }
            if (invalidCalculatedReadingType(constraintValidatorContext, readingType, numericalRegisterSpec.getCalculatedReadingType().get())) {
                return false;
            }
            if (deviceConfigAlreadyHasRegisterWithSuchCalculatedType(constraintValidatorContext, numericalRegisterSpec)) {
                return false;
            }
        }
        return true;
    }

    boolean deviceConfigAlreadyHasRegisterWithSuchCalculatedType(ConstraintValidatorContext constraintValidatorContext, NumericalRegisterSpecImpl numericalRegisterSpec) {
        List<RegisterSpec> allRegisterSpecs = numericalRegisterSpec.getDeviceConfiguration().getRegisterSpecs();
        for (RegisterSpec candidate : allRegisterSpecs) {
            if (candidate instanceof NumericalRegisterSpecImpl && numericalRegisterSpec.getId() != candidate.getId()) {
                if (((NumericalRegisterSpecImpl) candidate).getCalculatedReadingType().equals(numericalRegisterSpec.getCalculatedReadingType())) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_SPEC_DUPLICATE_REGISTER_TYPE + "}")
                            .addPropertyNode("calculatedReadingType")
                            .addConstraintViolation();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    String getReadingTypeFieldName() {
        return RegisterSpecFields.REGISTER_TYPE.fieldName();
    }
}
