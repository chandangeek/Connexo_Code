/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.pki.impl.wrappers.symmetric.HexBinary;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class HexBinaryValidator implements ConstraintValidator<HexBinary, String> {
    private String message;

    @Override
    public void initialize(HexBinary annotation) {
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        if (string.length() % 2 != 0){
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
