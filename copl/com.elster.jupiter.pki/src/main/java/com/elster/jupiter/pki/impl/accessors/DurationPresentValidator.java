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
 * Validate the KeyAccessorType has a duration is required, that is, in case of Passphrase of symmetric key
 */
public class DurationPresentValidator implements ConstraintValidator<DurationPresent, SecurityAccessorType> {
    private static final EnumSet<CryptographicType> EXPIREABLE = EnumSet.of(CryptographicType.Passphrase, CryptographicType.SymmetricKey, CryptographicType.Hsm);
    private String message;

    @Override
    public void initialize(DurationPresent durationPresent) {
        message = durationPresent.message();
    }

    @Override
    public boolean isValid(SecurityAccessorType securityAccessorType, ConstraintValidatorContext constraintValidatorContext) {
        if (EXPIREABLE.contains(securityAccessorType.getKeyType().getCryptographicType())) {
            if (!securityAccessorType.getDuration().isPresent()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(SecurityAccessorTypeImpl.Fields.DURATION.fieldName())
                .addConstraintViolation();
    }

}
