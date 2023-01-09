/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.pki.rest.SecurityAccessorResourceHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component(name = "com.elster.jupiter.pki.rest.impl.SecurityAccessorResourceHelperImpl",
        service = {SecurityAccessorResourceHelper.class},
        immediate = true)
public class SecurityAccessorResourceHelperImpl implements SecurityAccessorResourceHelper {
    private static final String ALIAS = "alias";
    private static final String TRUST_STORE = "trustStore";

    private volatile SecurityManagementService securityManagementService;
    private volatile PropertyValueInfoService propertyValueInfoService;

    public SecurityAccessorResourceHelperImpl() {
        // for OSGI
    }

    @Inject
    public SecurityAccessorResourceHelperImpl(SecurityManagementService securityManagementService,
                                              PropertyValueInfoService propertyValueInfoService) {
        setSecurityManagementService(securityManagementService);
        setPropertyValueInfoService(propertyValueInfoService);
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Override
    public CertificateWrapper createCertificateWrapper(SecurityAccessorType ignored, Map<String, Object> properties) {
        String alias = (String) properties.get(ALIAS);
        if (alias == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, ALIAS);
        }
        TrustStore trustStore = (TrustStore) properties.get(TRUST_STORE);
        if (trustStore == null) {
            return securityManagementService.findCertificateWrapper(alias)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_CERTIFICATE, ALIAS));
        }
        return trustStore.getCertificates().stream()
                .filter(cert -> cert.getAlias().equals(alias))
                .findAny()
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_CERTIFICATE_IN_STORE, ALIAS));
    }

    @Override
    public Optional<CertificateWrapper> createCertificateWrapper(SecurityAccessorType securityAccessorType, List<PropertyInfo> infos) {
        List<PropertySpec> propertySpecs = securityManagementService.getPropertySpecs(securityAccessorType);
        return Optional.of(propertyValueInfoService.findPropertyValues(propertySpecs, infos == null ? Collections.emptyList() : infos))
                .filter(SecurityAccessorResourceHelperImpl::propertiesContainValues)
                .map(properties -> createCertificateWrapper(securityAccessorType, properties));
    }

    @Override
    public CertificateWrapper createMandatoryCertificateWrapper(SecurityAccessorType securityAccessorType, List<PropertyInfo> infos) {
        List<PropertySpec> propertySpecs = securityManagementService.getPropertySpecs(securityAccessorType);
        Map<String, Object> propertyValues = propertyValueInfoService.findPropertyValues(propertySpecs, infos == null ? Collections.emptyList() : infos);
        return createCertificateWrapper(securityAccessorType, propertyValues);
    }

    @Override
    public boolean updateActualCertificateIfNeeded(SecurityAccessor<CertificateWrapper> securityAccessor, List<PropertyInfo> infos, boolean mandatory) {
        List<PropertySpec> propertySpecs = securityManagementService.getPropertySpecs(securityAccessor.getSecurityAccessorType());
        Map<String, Object> requested = Optional.of(propertyValueInfoService.findPropertyValues(propertySpecs, infos == null ? Collections.emptyList() : infos))
                .filter(SecurityAccessorResourceHelperImpl::propertiesContainValues)
                .orElseGet(Collections::emptyMap);
        Map<String, Object> current = securityAccessor.getActualValue()
                .map(CertificateWrapper::getProperties)
                .orElseGet(Collections::emptyMap);
        if (propertiesDiffer(requested, current)) {
            if (!mandatory && requested.isEmpty()) {
                securityAccessor.clearActualValue();
            } else {
                securityAccessor.setActualValue(createCertificateWrapper(securityAccessor.getSecurityAccessorType(), requested));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateTempCertificateIfNeeded(SecurityAccessor<CertificateWrapper> securityAccessor, List<PropertyInfo> infos) {
        List<PropertySpec> propertySpecs = securityManagementService.getPropertySpecs(securityAccessor.getSecurityAccessorType());
        Map<String, Object> requested = Optional.of(propertyValueInfoService.findPropertyValues(propertySpecs, infos == null ? Collections.emptyList() : infos))
                .filter(SecurityAccessorResourceHelperImpl::propertiesContainValues)
                .orElseGet(Collections::emptyMap);
        Map<String, Object> current = securityAccessor.getTempValue()
                .map(CertificateWrapper::getProperties)
                .orElseGet(Collections::emptyMap);
        if (propertiesDiffer(requested, current)) {
            if (requested.isEmpty()) {
                securityAccessor.clearTempValue();
            } else {
                securityAccessor.setTempValue(createCertificateWrapper(securityAccessor.getSecurityAccessorType(), requested));
            }
            return true;
        }
        return false;
    }

    private static boolean propertiesContainValues(Map<String, Object> properties) {
        return properties.values().stream().anyMatch(Objects::nonNull);
    }

    private static boolean propertiesDiffer(Map<String, Object> newProperties, Map<String, Object> existingProperties) {
        return !newProperties.equals(existingProperties); // hmm, will equals() work well for all property-types? well, it should, right?
    }

}
