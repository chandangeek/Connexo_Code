package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that you have a calculated readingtype when a multiplier is configured
 */
public class MultiplierConfigurationValidator implements ConstraintValidator<ValidMultiplierConfiguration, NumericalRegisterSpecImpl> {
    @Override
    public void initialize(ValidMultiplierConfiguration validMultiplierConfiguration) {

    }

    @Override
    public boolean isValid(NumericalRegisterSpecImpl numericalRegisterSpec, ConstraintValidatorContext constraintValidatorContext) {
        if (numericalRegisterSpec.isUseMultiplier() && !numericalRegisterSpec.getCalculatedReadingType().isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY + "}").addConstraintViolation();
            return false;
        }
        return true;
    }
}
