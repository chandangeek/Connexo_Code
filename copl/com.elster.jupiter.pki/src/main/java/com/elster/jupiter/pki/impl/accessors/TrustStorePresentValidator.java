/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.SecurityAccessorType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;

/**
 * Created by bvn on 3/13/17.
 */
public class TrustStorePresentValidator implements ConstraintValidator<TrustStorePresent, SecurityAccessorType> {
    private static final EnumSet<CryptographicType> CERTIFICATES = EnumSet.of(CryptographicType.Certificate, CryptographicType.ClientCertificate);
    private String message;

    @Override
    public void initialize(TrustStorePresent durationPresent) {
        message = durationPresent.message();
    }

    @Override
    public boolean isValid(SecurityAccessorType securityAccessorType, ConstraintValidatorContext constraintValidatorContext) {
        if (CERTIFICATES.contains(securityAccessorType.getKeyType().getCryptographicType())) {
            if (!securityAccessorType.getTrustStore().isPresent()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(SecurityAccessorTypeImpl.Fields.TRUSTSTORE.fieldName())
                .addConstraintViolation();
    }
}
