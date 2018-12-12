/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validate username and password are not empty if authentication is set to basic
 * Created by bvn on 6/13/16.
 */
public class ValidCredentialsValidator implements ConstraintValidator<ValidCredentials, OutboundEndPointConfigurationImpl> {

    @Inject
    public ValidCredentialsValidator() {
    }

    @Override
    public void initialize(ValidCredentials validCredentials) {

    }

    @Override
    public boolean isValid(OutboundEndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext context) {
        boolean valid = true;
        if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())) {
            if (Checks.is(endPointConfiguration.getUsername()).emptyOrOnlyWhiteSpace()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
                        .addPropertyNode(EndPointConfigurationImpl.Fields.USERNAME.fieldName())
                        .addConstraintViolation();
                valid = false;
            }
            if (Checks.is(endPointConfiguration.getPassword()).emptyOrOnlyWhiteSpace()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
                        .addPropertyNode(EndPointConfigurationImpl.Fields.PASSWD.fieldName()).addConstraintViolation();
                valid = false;
            }
        }
        return valid;
    }
}
