/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.energyict.mdc.device.configuration.rest.TrustStoreValuesProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component(name = "com.energyict.mdc.device.configuration.rest.impl.TrustStoreValuesProviderImpl",
        service = {TrustStoreValuesProvider.class},
        immediate = true)
public class TrustStoreValuesProviderImpl implements TrustStoreValuesProvider {
    private volatile SecurityManagementService securityManagementService;

    public TrustStoreValuesProviderImpl() {
        // for OSGI
    }

    @Inject
    public TrustStoreValuesProviderImpl(SecurityManagementService securityManagementService) {
        setSecurityManagementService(securityManagementService);
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Override
    public List<TrustStore> getPropertyPossibleValues(PropertySpec propertySpec, PropertyType propertyType) {
        if (propertySpec.getName().equals("trustStore")) {
            return securityManagementService.getAllTrustStores();
        }
        return Collections.emptyList();
    }
}
