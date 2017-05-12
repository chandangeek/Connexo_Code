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
 * Created by bvn on 3/13/17.
 */
public class TrustStorePresentValidator implements ConstraintValidator<TrustStorePresent, KeyAccessorType> {
    private static final EnumSet<CryptographicType> CERTIFICATES = EnumSet.of(CryptographicType.Certificate, CryptographicType.ClientCertificate);
    private String message;

    @Override
    public void initialize(TrustStorePresent durationPresent) {
        message = durationPresent.message();
    }

    @Override
    public boolean isValid(KeyAccessorType keyAccessorType, ConstraintValidatorContext constraintValidatorContext) {
        if (CERTIFICATES.contains(keyAccessorType.getKeyType().getCryptographicType())) {
            if (!keyAccessorType.getTrustStore().isPresent()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(KeyAccessorTypeImpl.Fields.TRUSTSTORE.fieldName())
                .addConstraintViolation();
    }

}
