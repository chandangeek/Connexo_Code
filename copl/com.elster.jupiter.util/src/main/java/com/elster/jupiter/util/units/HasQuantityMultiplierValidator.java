package com.elster.jupiter.util.units;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class HasQuantityMultiplierValidator implements ConstraintValidator<HasQuantityMultiplier, Quantity> {

    private HasQuantityMultiplier constraintAnnotation;

    @Override
    public void initialize(HasQuantityMultiplier constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Quantity value, ConstraintValidatorContext context) {
        if (value!=null) {
            if (value.getMultiplier() < constraintAnnotation.min()
                    || value.getMultiplier() > constraintAnnotation.max()) {
                return false;
            }
        }
        return true;
    }
}
