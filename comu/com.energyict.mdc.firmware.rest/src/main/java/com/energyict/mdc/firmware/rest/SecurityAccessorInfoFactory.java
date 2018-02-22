/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.TrustStore;

public class SecurityAccessorInfoFactory {

    public SecurityAccessorInfoFactory() {
        // for OSGI
    }

    public SecurityAccessorInfo from(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = securityAccessor.getKeyAccessorType().getId();
        info.name = securityAccessor.getKeyAccessorType().getName();
        info.description = securityAccessor.getKeyAccessorType().getDescription();
        if (securityAccessor.getKeyAccessorType().getTrustStore().isPresent()) {
            TrustStore trustStore = securityAccessor.getKeyAccessorType().getTrustStore().get();
            info.truststore = trustStore.getName();
        }
        if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            if (certificateWrapper.getExpirationTime().isPresent()) {
                info.expirationTime = certificateWrapper.getExpirationTime().get();
            }
        }
        return info;
    }

}