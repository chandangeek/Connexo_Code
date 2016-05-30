package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidComServerUriValidator implements ConstraintValidator<ValidComServerUri, ComServer> {

    private static final String INVALID_SERVERNAME_URI_PATTERN = "servername_missing";
    private static final String INVALID_PORT_URI_PATTERN = ":0/";

    private static final String SERVER_NAME_NODE = "serverName";
    private static final String EVENT_REGISTRATION_PORT_NODE = "eventRegistrationPort";
    private static final String MONITOR_PORT_NODE = "monitorPort";

    private String message;

    @Inject
    public ValidComServerUriValidator() {
    }

    @Override
    public void initialize(ValidComServerUri constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ComServer comServer, ConstraintValidatorContext context) {
        if (comServer == null) {
            return true;
        }

        if (comServer.isOnline()) {
            boolean valid = true;
            String eventRegistrationUri = ((OnlineComServer) comServer).getEventRegistrationUri();
            if (eventRegistrationUri != null && eventRegistrationUri.contains(INVALID_SERVERNAME_URI_PATTERN)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(SERVER_NAME_NODE).addConstraintViolation();
                valid = false;
            }
            if (eventRegistrationUri != null && eventRegistrationUri.contains(INVALID_PORT_URI_PATTERN)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(EVENT_REGISTRATION_PORT_NODE).addConstraintViolation();
                valid = false;
            }

            String statusUri = ((OnlineComServer) comServer).getStatusUri();
            if (statusUri != null && statusUri.contains(INVALID_SERVERNAME_URI_PATTERN)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(SERVER_NAME_NODE).addConstraintViolation();
                valid = false;
            }
            if (statusUri != null && statusUri.contains(INVALID_PORT_URI_PATTERN)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(MONITOR_PORT_NODE).addConstraintViolation();
                valid = false;

            }
            return valid;
        }
        return true;
    }
}