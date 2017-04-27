/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.pki.impl.wrappers.symmetric.KeySize;
import com.elster.jupiter.pki.impl.wrappers.symmetric.PlaintextSymmetricKeyImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class KeySizeValidator implements ConstraintValidator<KeySize, PlaintextSymmetricKeyImpl.PropertySetter> {
    private String message;

    @Override
    public void initialize(KeySize annotation) {
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(PlaintextSymmetricKeyImpl.PropertySetter propertySetter, ConstraintValidatorContext constraintValidatorContext) {
        try {
            byte[] decode = Base64.getDecoder().decode(propertySetter.getKey());
            if (decode.length*8!=propertySetter.getKeySize()) {
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(message)
                        .addPropertyNode("key")
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            return true; // this is checked elsewhere
        }
    }
}
