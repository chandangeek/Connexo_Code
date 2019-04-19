/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.CIMInboundSoapEndpointsActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterCustomPropertyPersistenceSupport;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterCustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.UsagePointConfigMasterCustomPropertySet", service = CustomPropertySet.class, property = "name="
        + UsagePointConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME, immediate = true)
public class UsagePointConfigMasterCustomPropertySet
        extends AbstractMasterCustomPropertySet<UsagePointConfigMasterDomainExtension>
        implements CustomPropertySet<ServiceCall, UsagePointConfigMasterDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "UsagePointConfigMasterCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public UsagePointConfigMasterCustomPropertySet() {
    }

    @Inject
    public UsagePointConfigMasterCustomPropertySet(PropertySpecService propertySpecService,
            CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        setPropertySpecService(propertySpecService);
        setCustomPropertySetService(customPropertySetService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public String getName() {
        return UsagePointConfigMasterCustomPropertySet.class.getSimpleName();
    }

    @Override
    public PersistenceSupport<ServiceCall, UsagePointConfigMasterDomainExtension> getPersistenceSupport() {
        return new UsagePointConfigMasterCustomPropertyPersistenceSupport();
    }

    private class UsagePointConfigMasterCustomPropertyPersistenceSupport
            extends AbstractMasterCustomPropertyPersistenceSupport<UsagePointConfigMasterDomainExtension> {

        private static final String TABLE_NAME = "UCP_MSC_WS1";
        private static final String FK = "FK_UCP_MSC_WS1";

        @Override
        public String componentName() {
            return "UCM";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<UsagePointConfigMasterDomainExtension> persistenceClass() {
            return UsagePointConfigMasterDomainExtension.class;
        }
    }
}
