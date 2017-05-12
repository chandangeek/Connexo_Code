/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;

/**
 * Validate the KeyAccessorType has a duration is required, that is, in case of Passphrase of symmetric key
 */
public class DurationPresentValidator implements ConstraintValidator<DurationPresent, KeyAccessorType> {
    private static final EnumSet<CryptographicType> EXPIREABLE = EnumSet.of(CryptographicType.Passphrase, CryptographicType.SymmetricKey);
    private String message;

    @Override
    public void initialize(DurationPresent durationPresent) {
        message = durationPresent.message();
    }

    @Override
    public boolean isValid(KeyAccessorType keyAccessorType, ConstraintValidatorContext constraintValidatorContext) {
        if (EXPIREABLE.contains(keyAccessorType.getKeyType().getCryptographicType())) {
            if (!keyAccessorType.getDuration().isPresent()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(KeyAccessorTypeImpl.Fields.DURATION.fieldName())
                .addConstraintViolation();
    }

}
