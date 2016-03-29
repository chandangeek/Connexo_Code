package com.elster.jupiter.util.units;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasQuantityUnitValidator implements ConstraintValidator<HasQuantityUnit, Quantity> {

    private HasQuantityUnit constraintAnnotation;

    @Override
    public void initialize(HasQuantityUnit constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Quantity value, ConstraintValidatorContext context) {

        if(value != null) {
            if (value.getUnit() != null) {
                for (Unit unit : constraintAnnotation.units()) {
                    if (value.getUnit().equals(unit)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }
}
