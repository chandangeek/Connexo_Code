/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;

import java.sql.Date;

public class SecurityAccessorInfoFactory {

    public SecurityAccessorInfoFactory() {
        // for OSGI
    }

    private static final String NOT_DEFINED = "Not Defined";

    public SecurityAccessorInfo from(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = securityAccessor.getKeyAccessorTypeReference().getId();
        info.name = securityAccessor.getKeyAccessorTypeReference().getName();
        info.type = securityAccessor.getKeyAccessorTypeReference().getKeyType().getName();
        info.description = securityAccessor.getKeyAccessorTypeReference().getDescription();
        info.truststore = securityAccessor.getKeyAccessorTypeReference().getTrustStore().isPresent() ?
                securityAccessor.getKeyAccessorTypeReference().getTrustStore().get().getName() :
                NOT_DEFINED;
        info.certificate = securityAccessor.getActualPassphraseWrapperReference()
                .filter(e -> e instanceof CertificateWrapper)
                .map(CertificateWrapper.class::cast)
                .map(CertificateWrapper::getAlias)
                .orElse(null);
        if (securityAccessor.getActualPassphraseWrapperReference().isPresent() && securityAccessor.getActualPassphraseWrapperReference().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualPassphraseWrapperReference().get();
            info.expirationTime = certificateWrapper.getExpirationTime().isPresent() ?
                    Date.from(certificateWrapper.getExpirationTime().get()).toString() :
                    NOT_DEFINED;
        } else {
            info.expirationTime = NOT_DEFINED;
        }
        return info;
    }

}