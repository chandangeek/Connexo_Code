/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueComServerNameValidator implements ConstraintValidator<UniqueName, ComServer> {

    private String message;
    private EngineConfigurationService engineConfigurationService;

    @Inject
    public UniqueComServerNameValidator(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
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
        Optional<ComServer> comServerWithTheSameName = engineConfigurationService.findComServer(comServer.getName());
        if (comServerWithTheSameName.isPresent() && comServer.getId() != comServerWithTheSameName.get().getId() && !comServerWithTheSameName.get().isObsolete()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComServerImpl.FieldNames.NAME.getName()).addConstraintViolation();
            return false;
        }
        return true;
    }

}