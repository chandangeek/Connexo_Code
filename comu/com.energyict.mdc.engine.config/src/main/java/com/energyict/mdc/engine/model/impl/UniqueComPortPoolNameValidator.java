package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComPortPoolNameValidator implements ConstraintValidator<UniqueName, ComPortPool> {

    private String message;
    private EngineModelService engineModelService;

    @Inject
    public UniqueComPortPoolNameValidator(EngineModelService engineModelService) {
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
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComPortPoolImpl.FieldNames.NAME.getName()).addConstraintViolation();
                return false;
            }
        return true;
    }

}
