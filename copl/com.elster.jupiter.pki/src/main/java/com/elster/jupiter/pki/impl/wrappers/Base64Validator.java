/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.pki.impl.wrappers.symmetric.Base64EncodedKey;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class Base64Validator implements ConstraintValidator<Base64EncodedKey, String> {
    private String message;

    @Override
    public void initialize(Base64EncodedKey annotation) {
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Base64.getDecoder().decode(string);
        } catch (IllegalArgumentException e) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
