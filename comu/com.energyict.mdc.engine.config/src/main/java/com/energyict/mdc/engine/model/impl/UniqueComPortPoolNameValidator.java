package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPortPool;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComPortPoolNameValidator implements ConstraintValidator<UniqueName, ComPortPool> {

    private String message;
    private EngineModelServiceImpl engineModelService;

    @Inject
    public UniqueComPortPoolNameValidator(EngineModelServiceImpl engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComPortPool comPortPoolUnderEvaluation, ConstraintValidatorContext context) {
        ComPortPool comPortPool = engineModelService.findComPortPool(comPortPoolUnderEvaluation.getName());
            if (comPortPool != null && comPortPool.getId()!=comPortPoolUnderEvaluation.getId() && !comPortPool.isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return false;
            }
        return true;
    }

}
