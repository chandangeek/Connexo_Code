/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueComPortPoolNameValidator implements ConstraintValidator<UniqueName, ComPortPool> {

    private String message;
    private EngineConfigurationService engineConfigurationService;

    @Inject
    public UniqueComPortPoolNameValidator(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComPortPool comPortPoolUnderEvaluation, ConstraintValidatorContext context) {
        Optional<? extends ComPortPool> comPortPool = engineConfigurationService.findComPortPoolByName(comPortPoolUnderEvaluation.getName());
        if (this.isPresent(comPortPool) && comPortPool.get().getId() != comPortPoolUnderEvaluation.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(ComPortPoolImpl.Fields.NAME.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isPresent(Optional<? extends ComPortPool> comPortPool) {
        return comPortPool.isPresent() && !comPortPool.get().isObsolete();
    }

}
