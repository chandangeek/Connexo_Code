/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

@Component(name = MasterUtilitiesDeviceCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        service = CustomPropertySet.class,
        property = "name=" + MasterUtilitiesDeviceCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MasterUtilitiesDeviceCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MasterUtilitiesDeviceCreateRequestDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MasterUtilitiesDeviceCreateRequestCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterUtilitiesDeviceCreateRequestCustomPropertySet() {
    }

    @Inject
    public MasterUtilitiesDeviceCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getName() {
        return MasterUtilitiesDeviceCreateRequestCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, MasterUtilitiesDeviceCreateRequestDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .stringSpec()
                        .named(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_UUID)
                        .describedAs(TranslationKeys.REQUEST_UUID)
                        .fromThesaurus(thesaurus)
                        .finish()

        );
    }
    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterUtilitiesDeviceCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_UD5";
        private final String FK = "FK_SAP_CPS_UD5";

        @Override
        public String componentName() {
            return "UD5";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterUtilitiesDeviceCreateRequestDomainExtension> persistenceClass() {
            return MasterUtilitiesDeviceCreateRequestDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
