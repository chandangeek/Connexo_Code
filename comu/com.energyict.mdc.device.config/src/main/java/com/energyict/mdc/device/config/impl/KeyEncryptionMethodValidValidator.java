/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;

/**
 * Created by bvn on 3/13/17.
 */
public class KeyEncryptionMethodValidValidator implements ConstraintValidator<KeyEncryptionMethodValid, KeyAccessorType> {
    private static final EnumSet<CryptographicType> SECRETS = EnumSet.of(CryptographicType.AsymmetricKey, CryptographicType.Passphrase, CryptographicType.SymmetricKey);
    private String message;

    @Override
    public void initialize(KeyEncryptionMethodValid keyEncryptionMethodValid) {
        message = keyEncryptionMethodValid.message();
    }

    @Override
    public boolean isValid(KeyAccessorType keyAccessorType, ConstraintValidatorContext constraintValidatorContext) {
        if (SECRETS.contains(keyAccessorType.getKeyType().getCryptographicType())) {
            if (Checks.is(keyAccessorType.getKeyEncryptionMethod()).emptyOrOnlyWhiteSpace()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(KeyAccessorTypeImpl.Fields.ENCRYPTIONMETHOD.fieldName())
                .addConstraintViolation();
    }

}
