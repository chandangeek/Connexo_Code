/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;

/**
 * Validate the KeyEncryptionMethod is present on KeyAccessorTypes that require one: Symmetric key, Asymmetric keys,
 * passphrases and ClientCertificates (the latter have a private key attached)
 */
public class KeyEncryptionMethodValidValidator implements ConstraintValidator<KeyEncryptionMethodValid, SecurityAccessorType> {
    private static final EnumSet<CryptographicType> SECRETS = EnumSet.of(CryptographicType.AsymmetricKey, CryptographicType.Passphrase,
            CryptographicType.SymmetricKey, CryptographicType.ClientCertificate, CryptographicType.Hsm);
    private String message;

    @Override
    public void initialize(KeyEncryptionMethodValid keyEncryptionMethodValid) {
        message = keyEncryptionMethodValid.message();
    }

    @Override
    public boolean isValid(SecurityAccessorType securityAccessorType, ConstraintValidatorContext constraintValidatorContext) {
        if (SECRETS.contains(securityAccessorType.getKeyType().getCryptographicType())) {
            if (Checks.is(securityAccessorType.getKeyEncryptionMethod()).emptyOrOnlyWhiteSpace()) {
                fail(constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(SecurityAccessorTypeImpl.Fields.ENCRYPTIONMETHOD.fieldName())
                .addConstraintViolation();
    }
}
