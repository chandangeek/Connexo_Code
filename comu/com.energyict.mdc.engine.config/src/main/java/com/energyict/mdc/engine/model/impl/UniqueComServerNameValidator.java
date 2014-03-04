package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComServerNameValidator implements ConstraintValidator<UniqueName, ComServer> {

    private String message;
    private EngineModelServiceImpl engineModelService;

    @Inject
    public UniqueComServerNameValidator(EngineModelServiceImpl engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComServer comServer, ConstraintValidatorContext context) {
        if (comServer==null) {
            return true;
        }
        ComServer comServerWithTheSameName = engineModelService.findComServer(comServer.getName());
        if (comServerWithTheSameName != null && comServer.getId() != comServerWithTheSameName.getId() && !comServerWithTheSameName.isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return false;
            }
        return true;
    }

}
