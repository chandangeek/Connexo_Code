package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.google.inject.Inject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OverFlowAndNumberOfFractionDigitsValidator implements ConstraintValidator<ValidOverFlowAndNumberOfFractionDigits, NumericalRegisterSpecImpl> {

    private Thesaurus thesaurus;

    @Inject
    public OverFlowAndNumberOfFractionDigitsValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(ValidOverFlowAndNumberOfFractionDigits constraintAnnotation) {
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
        return valid;
    }
}
