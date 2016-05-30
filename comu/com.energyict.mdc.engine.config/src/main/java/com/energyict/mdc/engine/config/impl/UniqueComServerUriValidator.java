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
    private static final String MONITOR_PORT = "monitorPort";
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
            Optional<ComServer> comServerWithTheSameEventRegistrationUri = engineConfigurationService.findComServerByEventRegistrationUri(((OnlineComServer) comServer).getEventRegistrationUri());
            if (comServerWithTheSameEventRegistrationUri.isPresent()
                    && comServer.getId() != comServerWithTheSameEventRegistrationUri.get().getId()
                    && !comServerWithTheSameEventRegistrationUri.get().isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(EVENT_REGISTRATION_PORT).addConstraintViolation();
                successful = false;
            }

            Optional<ComServer> comServerWithTheSameStatusUri = engineConfigurationService.findComServerByStatusUri(((OnlineComServer) comServer).getStatusUri());
            if (comServerWithTheSameStatusUri.isPresent()
                    && comServer.getId() != comServerWithTheSameStatusUri.get().getId()
                    && !comServerWithTheSameStatusUri.get().isObsolete()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(MONITOR_PORT).addConstraintViolation();
                successful = false;
            }
        }

        return successful;
    }
}