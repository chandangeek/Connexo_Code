package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 26.01.16
 * Time: 13:19
 */
public class RegisterOverflowValueValidator implements ConstraintValidator<RegisterOverflowValueValidation, NumericalRegisterSpecImpl> {
    @Override
    public void initialize(RegisterOverflowValueValidation registerOverflowValueValidation) {

    }

    @Override
    public boolean isValid(NumericalRegisterSpecImpl registerSpec, ConstraintValidatorContext constraintValidatorContext) {
            if (registerSpec.getReadingType().isCumulative() && !registerSpec.getOverflowValue().isPresent()) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
                        .addPropertyNode("overflow")
                        .addConstraintViolation();
                return false;
            }
            if (registerSpec.getOverflowValue().isPresent() && registerSpec.getOverflowValue().get().compareTo(BigDecimal.ZERO) < 1) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE + "}")
                        .addPropertyNode("overflow")
                        .addConstraintViolation();
                return false;
            }
        return true;
    }
}
