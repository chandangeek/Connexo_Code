/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

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
        Optional<CertificateWrapper> namesake = dataModel.mapper(CertificateWrapper.class).getUnique(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName(), certificateWrapper.getAlias());
        namesake.ifPresent(certificateWrapper1 -> {
            if (certificateWrapper1.getId() != certificateWrapper.getId()) {
                fail(constraintValidatorContext);
            }
        });
        return true;
    }

    private boolean fail(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                .addConstraintViolation();
        return false; // something is not valid
    }

}
