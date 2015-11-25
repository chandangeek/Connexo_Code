package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.ReadingType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
            if (invalidCalculatedReadingType(constraintValidatorContext, readingType, numericalRegisterSpec.getCalculatedReadingType().get())){
                return false;
            }
        }
        return true;
    }

    @Override
    String getReadingTypeFieldName() {
        return RegisterSpecFields.REGISTER_TYPE.fieldName();
    }
}
