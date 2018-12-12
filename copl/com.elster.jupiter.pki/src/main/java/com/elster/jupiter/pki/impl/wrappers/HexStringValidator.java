/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.pki.impl.wrappers.symmetric.HexStringKey;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigInteger;

public class HexStringValidator implements ConstraintValidator<HexStringKey, String> {
    private String message;

    @Override
    public void initialize(HexStringKey annotation) {
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        try {
            new BigInteger(string.toUpperCase(), 16);
        } catch (NumberFormatException e) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
