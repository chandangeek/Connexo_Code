package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComPortPoolNameValidator implements ConstraintValidator<UniqueName, ComPortPool> {

    private String message;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComPortPool comPortPoolUnderEvaluation, ConstraintValidatorContext context) {
        for (ComPort comPort : comPortPoolUnderEvaluation.getComPorts()) {
            if (comPort.getId()!=comPortPoolUnderEvaluation.getId() && comPort.getName().equals(comPortPoolUnderEvaluation.getName()) && !comPort.isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}
