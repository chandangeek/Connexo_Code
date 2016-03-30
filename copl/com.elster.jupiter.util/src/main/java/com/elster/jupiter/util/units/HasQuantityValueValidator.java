package com.elster.jupiter.util.units;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class HasQuantityValueValidator implements ConstraintValidator<HasQuantityValue, Quantity> {

    private HasQuantityValue constraintAnnotation;

    @Override
    public void initialize(HasQuantityValue constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Quantity value, ConstraintValidatorContext context) {
        return value == null
                || (value.getValue().compareTo(BigDecimal.valueOf(constraintAnnotation.min())) < 0
                && value.getValue().compareTo(BigDecimal.valueOf(constraintAnnotation.max())) > 0);
    }

}
