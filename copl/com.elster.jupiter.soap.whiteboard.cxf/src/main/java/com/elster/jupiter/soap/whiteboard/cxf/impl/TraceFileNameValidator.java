package com.elster.jupiter.soap.whiteboard.cxf.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.FilePermission;

/**
 * Created by bvn on 6/13/16.
 */
public class TraceFileNameValidator implements ConstraintValidator<ValidTarceFileName, EndPointConfigurationImpl> {

    @Override
    public void initialize(ValidTarceFileName validTarceFileName) {

    }

    @Override
    public boolean isValid(EndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext constraintValidatorContext) {
        if (endPointConfiguration.isTracing()) {
            String basedir = File.separatorChar + "xyz" + File.separatorChar; // bogus path for validation purposes, not real security
            FilePermission sandbox = new FilePermission(basedir + "-", "write");
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
