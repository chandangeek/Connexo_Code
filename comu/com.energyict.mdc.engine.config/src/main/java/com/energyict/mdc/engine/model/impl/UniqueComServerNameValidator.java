package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;

import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueComServerNameValidator implements ConstraintValidator<UniqueName, ComServer> {

    private String message;
    private EngineModelService engineModelService;

    @Inject
    public UniqueComServerNameValidator(EngineModelService engineModelService) {
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
        Optional<ComServer> comServerWithTheSameName = engineModelService.findComServer(comServer.getName());
        if (comServerWithTheSameName.isPresent() && comServer.getId() != comServerWithTheSameName.get().getId() && !comServerWithTheSameName.get().isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComServerImpl.FieldNames.NAME.getName()).addConstraintViolation();
                return false;
            }
        return true;
    }

}
