package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.google.inject.Inject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class OverFlowAndNumberOfDigitsValidator implements ConstraintValidator<ValidOverFlowAndNumberOfDigits, NumericalRegisterSpecImpl> {

    private Thesaurus thesaurus;

    @Inject
    public OverFlowAndNumberOfDigitsValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(ValidOverFlowAndNumberOfDigits constraintAnnotation) {
    }

    @Override
    public boolean isValid(NumericalRegisterSpecImpl registerSpec, ConstraintValidatorContext context) {
        boolean valid=true;
        if (registerSpec.getOverflowValue() != null && registerSpec.hasNumberOfFractionDigits()) {
            int scale = registerSpec.getOverflowValue().scale();
            if (scale > registerSpec.getNumberOfFractionDigits()) {
                valid=false;
                String message = thesaurus.getFormat(MessageSeeds.REGISTER_SPEC_OVERFLOW_INCORRECT_FRACTION_DIGITS).format(registerSpec.getOverflowValue(), scale, registerSpec.getNumberOfFractionDigits());
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).
                        addPropertyNode(RegisterSpecFields.OVERFLOW_VALUE.fieldName()).
                        addConstraintViolation();
            }
        }
        if (registerSpec.getOverflowValue() != null && registerSpec.getNumberOfDigits() > 0) {
            if (registerSpec.getOverflowValue().compareTo(BigDecimal.valueOf(10).pow(registerSpec.getNumberOfDigits())) == 1) {
                valid=false;
                String message = thesaurus.getFormat(MessageSeeds.REGISTER_SPEC_OVERFLOW_LARGER_THAN_NUMBER_OF_DIGITS).format(registerSpec.getOverflowValue(), Math.pow(10, registerSpec.getNumberOfDigits()), registerSpec.getNumberOfDigits());
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).
                        addPropertyNode(RegisterSpecFields.OVERFLOW_VALUE.fieldName()).
                        addConstraintViolation();
            }
        }
        return valid;
    }
}
