/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.rest.PropertyInfo;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface SecurityAccessorResourceHelper {
    CertificateWrapper createCertificateWrapper(SecurityAccessorType ignored, Map<String, Object> properties);

    Optional<CertificateWrapper> createCertificateWrapper(SecurityAccessorType securityAccessorType, List<PropertyInfo> infos);

    CertificateWrapper createMandatoryCertificateWrapper(SecurityAccessorType securityAccessorType, List<PropertyInfo> infos);

    boolean updateActualCertificateIfNeeded(SecurityAccessor<CertificateWrapper> securityAccessor, List<PropertyInfo> infos, boolean mandatory);

    boolean updateTempCertificateIfNeeded(SecurityAccessor<CertificateWrapper> securityAccessor, List<PropertyInfo> infos);
}
