/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;


/**
 * The alias of a certificate wrapper needs to be unique...
 * - within a single truststore
 * - outside any truststore
 */
public class UniqueCertificateAliasValidator implements ConstraintValidator<UniqueAlias,CertificateWrapper> {

    private final DataModel dataModel;
    private String message;

    @Inject
    public UniqueCertificateAliasValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(UniqueAlias uniqueName) {
        this.message = uniqueName.message();
    }

    @Override
    public boolean isValid(CertificateWrapper certificateWrapper, ConstraintValidatorContext constraintValidatorContext) {
        List<CertificateWrapper> namesakes = dataModel.mapper(CertificateWrapper.class).find(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName(), certificateWrapper.getAlias());
        for (CertificateWrapper namesake: namesakes) {
            if (namesake.getId() != certificateWrapper.getId()) {
                if (isInTrustStore(namesake) && isInTrustStore(certificateWrapper)) {
                    if (((TrustedCertificate)namesake).getTrustStore().getId() == ((TrustedCertificate)certificateWrapper).getTrustStore().getId()) {
                        fail(constraintValidatorContext);
                        return false; // same alias in same truststore
                    }
                } else {
                    if (!isInTrustStore(namesake) && !isInTrustStore(certificateWrapper)) {
                        fail(constraintValidatorContext);
                        return false; // Same alias, both certs are global
                    }
                }
            }
        }
        return true;
    }

    private boolean isInTrustStore(CertificateWrapper certificateWrapper) {
        return TrustedCertificate.class.isAssignableFrom(certificateWrapper.getClass());
    }

    private void fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                .addConstraintViolation();
    }

}
