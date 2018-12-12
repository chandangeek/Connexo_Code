/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueComServerUriValidator implements ConstraintValidator<UniqueUri, ComServer> {

    private static final String EVENT_REGISTRATION_PORT = "eventRegistrationPort";
    private static final String STATUS_PORT = "statusPort";
    private String message;
    private EngineConfigurationService engineConfigurationService;

    @Inject
    public UniqueComServerUriValidator(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    public void initialize(UniqueUri constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComServer comServer, ConstraintValidatorContext context) {
        if (comServer == null) {
            return true;
        }

        boolean successful = true;
        if (comServer.isOnline()) {
            OnlineComServer onlineComServer = (OnlineComServer) comServer;
            Optional<ComServer> comServerWithTheSameEventRegistrationUri = engineConfigurationService.findComServerByServerNameAndEventRegistrationPort(onlineComServer.getServerName(), onlineComServer.getEventRegistrationPort());
            if (comServerWithTheSameEventRegistrationUri.isPresent()
                    && comServer.getId() != comServerWithTheSameEventRegistrationUri.get().getId()
                    && !comServerWithTheSameEventRegistrationUri.get().isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(EVENT_REGISTRATION_PORT).addConstraintViolation();
                successful = false;
            }

            Optional<ComServer> comServerWithTheSameStatusUri = engineConfigurationService.findComServerByServerNameAndStatusPort(onlineComServer.getServerName(), onlineComServer.getStatusPort());
            if (comServerWithTheSameStatusUri.isPresent()
                    && comServer.getId() != comServerWithTheSameStatusUri.get().getId()
                    && !comServerWithTheSameStatusUri.get().isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(STATUS_PORT).addConstraintViolation();
                successful = false;
            }
        }

        return successful;
    }
}