/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.FilePermission;

/**
 * Created by bvn on 6/13/16.
 */
public class TraceFileNameValidator implements ConstraintValidator<ValidTraceFileName, EndPointConfigurationImpl> {

    @Override
    public void initialize(ValidTraceFileName validTarceFileName) {

    }

    @Override
    public boolean isValid(EndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext constraintValidatorContext) {
        if (endPointConfiguration.isTracing()) {
            if (Checks.is(endPointConfiguration.getTraceFile()).emptyOrOnlyWhiteSpace()) {
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
                        .addPropertyNode(EndPointConfigurationImpl.Fields.TRACEFILE.fieldName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
            String basedir = File.separatorChar + "xyz" + File.separatorChar; // bogus path for validation purposes, not real security
            FilePermission sandbox = new FilePermission(basedir + "*", "write");
            FilePermission request = new FilePermission(basedir + File.separatorChar + endPointConfiguration.getTraceFile(), "write");
            if (!sandbox.implies(request)) {
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(EndPointConfigurationImpl.Fields.TRACEFILE.fieldName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
