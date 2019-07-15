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

@Component(name = MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        service = CustomPropertySet.class,
        property = "name=" + MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MasterUtilitiesDeviceRegisterCreateRequestDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet() {
    }

    @Inject
    public MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
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
        return MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MasterUtilitiesDeviceRegisterCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_UUID)
                        .describedAs(TranslationKeys.REQUEST_UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.BULK.javaName(), TranslationKeys.BULK)
                        .describedAs(TranslationKeys.BULK)
                        .fromThesaurus(thesaurus)
                        .finish()

        );
    }
    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterUtilitiesDeviceRegisterCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_UD3";
        private final String FK = "FK_SAP_CPS_UD3";

        @Override
        public String componentName() {
            return "UD3";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterUtilitiesDeviceRegisterCreateRequestDomainExtension> persistenceClass() {
            return MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class;
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
            table.column(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .add();
            table.column(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.BULK.databaseName())
                    .bool()
                    .map(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.BULK.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
