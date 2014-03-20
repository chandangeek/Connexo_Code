package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPort;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComPortNameValidator implements ConstraintValidator<UniqueName, ComPort> {

    private String message;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message=constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComPort comPortUnderEvaluation, ConstraintValidatorContext context) {
        for (ComPort comPort : comPortUnderEvaluation.getComServer().getComPorts()) {
            if (comPort.getId()!=comPortUnderEvaluation.getId() && comPort.getName().equals(comPortUnderEvaluation.getName()) && !comPort.isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComPortImpl.FieldNames.NAME.getName()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}
