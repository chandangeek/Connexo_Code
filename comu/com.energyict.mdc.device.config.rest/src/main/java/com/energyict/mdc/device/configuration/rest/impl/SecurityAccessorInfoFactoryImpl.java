/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;
import com.energyict.mdc.upl.TypedProperties;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.device.configuration.rest.impl.SecurityAccessorInfoFactoryImpl",
        service = {SecurityAccessorInfoFactory.class},
        immediate = true)
public class SecurityAccessorInfoFactoryImpl implements SecurityAccessorInfoFactory {
    private volatile MdcPropertyUtils mdcPropertyUtils;
    private volatile SecurityManagementService securityManagementService;

    public SecurityAccessorInfoFactoryImpl() {
        // for OSGI
    }

    @Inject
    public SecurityAccessorInfoFactoryImpl(MdcPropertyUtils mdcPropertyUtils, SecurityManagementService securityManagementService) {
        setMdcPropertyUtils(mdcPropertyUtils);
        setSecurityManagementService(securityManagementService);
    }

    @Reference
    public void setMdcPropertyUtils(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Override
    public SecurityAccessorInfo from(SecurityAccessor<?> securityAccessor) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = securityAccessor.getSecurityAccessorType().getId();
        info.name = securityAccessor.getSecurityAccessorType().getName();
        info.description = securityAccessor.getSecurityAccessorType().getDescription();
        info.swapped = securityAccessor.isSwapped();
        info.version = securityAccessor.getVersion();
        info.modificationDate = securityAccessor.getModTime();
        info.canGeneratePassiveKey = true;
        info.hasTempValue = securityAccessor.getTempValue().isPresent();
        info.hasActualValue = securityAccessor.getActualValue().isPresent();
        securityAccessor.getActualValue().ifPresent(ka -> ka.getExpirationTime().ifPresent(expiration -> info.expirationTime = expiration));
        return info;
    }

    private SecurityAccessorInfo setProperties(SecurityAccessorInfo info, List<PropertySpec> propertySpecs,
                                               TypedProperties currentProperties, TypedProperties tempProperties,
                                               PropertyValuesResourceProvider aliasTypeAheadPropertyResourceProvider,
                                               PropertyDefaultValuesProvider trustStoreValuesProvider) {
        info.currentProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, currentProperties, aliasTypeAheadPropertyResourceProvider, trustStoreValuesProvider);
        info.currentProperties.sort((PropertyInfo info1, PropertyInfo info2) -> info1.key.equals("trustStore") ? -1 : 0);
        info.tempProperties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(propertySpecs, tempProperties, aliasTypeAheadPropertyResourceProvider, trustStoreValuesProvider);
        info.tempProperties.sort((PropertyInfo info1, PropertyInfo info2) -> info1.key.equals("trustStore") ? -1 : 0);
        return info;
    }

    @Override
    public SecurityAccessorInfo asCertificateProperties(List<PropertySpec> propertySpecs,
                                                        PropertyValuesResourceProvider aliasTypeAheadPropertyResourceProvider,
                                                        PropertyDefaultValuesProvider trustStoreValuesProvider) {
        return setProperties(new SecurityAccessorInfo(), propertySpecs,
                TypedProperties.empty(), TypedProperties.empty(),
                aliasTypeAheadPropertyResourceProvider, trustStoreValuesProvider);
    }

    @Override
    public SecurityAccessorInfo asCertificate(SecurityAccessor<?> securityAccessor,
                                              PropertyValuesResourceProvider aliasTypeAheadPropertyResourceProvider,
                                              PropertyDefaultValuesProvider trustStoreValuesProvider) {
        SecurityAccessorInfo info = setProperties(from(securityAccessor), securityManagementService.getPropertySpecs(securityAccessor.getSecurityAccessorType()),
                getPropertiesActualValue(securityAccessor), getPropertiesTempValue(securityAccessor),
                aliasTypeAheadPropertyResourceProvider, trustStoreValuesProvider);
        if (securityAccessor instanceof CertificateAccessor) {
            ((CertificateAccessor) securityAccessor).getActualValue()
                    .ifPresent(cw -> cw.getLastReadDate().ifPresent(date -> info.lastReadDate = date));
        }
        return info;
    }

    @Override
    public TypedProperties getPropertiesTempValue(SecurityAccessor<?> securityAccessor) {
        TypedProperties tempTypedProperties = TypedProperties.empty();
        securityAccessor.getTempValue().ifPresent(ka -> ka.getProperties().forEach(tempTypedProperties::setProperty));
        return tempTypedProperties;
    }

    @Override
    public TypedProperties getPropertiesActualValue(SecurityAccessor<?> securityAccessor) {
        TypedProperties actualTypedProperties = TypedProperties.empty();
        securityAccessor.getActualValue().ifPresent(ka -> ka.getProperties().forEach(actualTypedProperties::setProperty));
        return actualTypedProperties;
    }
}
