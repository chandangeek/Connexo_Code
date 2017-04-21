/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.CryptographicType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validate that IF the KeyType is a Passphrase, it is valid
 */
public class ValidatePassphraseTypeValidator implements ConstraintValidator<ValidatePassphraseType, KeyTypeImpl> {

    @Override
    public void initialize(ValidatePassphraseType validatePassphraseType) {
    }

    @Override
    public boolean isValid(KeyTypeImpl keyType, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid = true;
        if (keyType.getCryptographicType().equals(CryptographicType.Passphrase)) {
            if (!keyType.useLowerCaseCharacters() && !keyType.useUpperCaseCharacters() && !keyType.useNumbers() && !keyType.useSpecialCharacters()) {
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("{"+MessageSeeds.Keys.NOVALIDCHARACTERS+"}")
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                valid=false;
            }
            if (keyType.getPasswordLength()==null || keyType.getPasswordLength()<=0) {
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("{"+MessageSeeds.Keys.INVALIDPASSPHRASELENGTH+"}")
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                valid=false;
            }
        }
        return valid;
    }
}
