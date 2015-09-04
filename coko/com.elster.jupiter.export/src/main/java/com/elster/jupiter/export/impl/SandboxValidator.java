package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.nls.LocalizedFieldValidationException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.FilePermission;

/**
 * Created by bvn on 9/3/15.
 */
public class SandboxValidator implements ConstraintValidator<Sandboxed, FileDestination> {
    private String message;

    @Override
    public void initialize(Sandboxed sandboxed) {
        message = sandboxed.message();
    }

    @Override
    public boolean isValid(FileDestination fileDestination, ConstraintValidatorContext constraintValidatorContext) {
        boolean success=verifySandboxBreaking("", fileDestination.getFileName(), "csv", "fileName", constraintValidatorContext);
        success&=verifySandboxBreaking("", "_", fileDestination.getFileExtension(), "fileExtension", constraintValidatorContext);
        success&=verifySandboxBreaking(fileDestination.getFileLocation(), "_", "csv", "fileLocation", constraintValidatorContext);
        return success;
    }

    private boolean verifySandboxBreaking(String path, String prefix, String extension, String property, ConstraintValidatorContext constraintValidatorContext) {
        String basedir = File.separatorChar + "xyz" + File.separatorChar; // bogus path for validation purposes, not real security
        FilePermission sandbox = new FilePermission(basedir+"-", "write");
        FilePermission request = new FilePermission(basedir + path + File.separatorChar + prefix + "A." + extension, "write");
        if (!sandbox.implies(request)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("properties."+ property)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }

}

