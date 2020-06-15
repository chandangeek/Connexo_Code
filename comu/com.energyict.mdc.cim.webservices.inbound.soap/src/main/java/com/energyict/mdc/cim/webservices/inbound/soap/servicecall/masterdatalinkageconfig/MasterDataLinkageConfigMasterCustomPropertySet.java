/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterCustomPropertyPersistenceSupport;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterCustomPropertySet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.MasterDataLinkageConfigMasterCustomPropertySet", service = CustomPropertySet.class, property = "name="
        + MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME, immediate = true)
public class MasterDataLinkageConfigMasterCustomPropertySet
        extends AbstractMasterCustomPropertySet<MasterDataLinkageConfigMasterDomainExtension>
        implements CustomPropertySet<ServiceCall, MasterDataLinkageConfigMasterDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "MasterDataLinkageConfigMasterCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterDataLinkageConfigMasterCustomPropertySet() {
    }

    @Inject
    public MasterDataLinkageConfigMasterCustomPropertySet(PropertySpecService propertySpecService,
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
        thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public PropertySpecService getPropertySpecService() {
        return this.propertySpecService;
    }

    @Override
    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return MasterDataLinkageConfigMasterCustomPropertySet.class.getSimpleName();
    }

    @Override
    public PersistenceSupport<ServiceCall, MasterDataLinkageConfigMasterDomainExtension> getPersistenceSupport() {
        return new MasterDataLinkageConfigMasterCustomPropertyPersistenceSupport();
    }

    private class MasterDataLinkageConfigMasterCustomPropertyPersistenceSupport
            extends AbstractMasterCustomPropertyPersistenceSupport<MasterDataLinkageConfigMasterDomainExtension> {

        private static final String TABLE_NAME = "DLP_MSC_WS1";
        private static final String FK = "FK_DLP_MSC_WS1";

        @Override
        public String componentName() {
            return "DLM";
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
        public Class<MasterDataLinkageConfigMasterDomainExtension> persistenceClass() {
            return MasterDataLinkageConfigMasterDomainExtension.class;
        }
    }
}
