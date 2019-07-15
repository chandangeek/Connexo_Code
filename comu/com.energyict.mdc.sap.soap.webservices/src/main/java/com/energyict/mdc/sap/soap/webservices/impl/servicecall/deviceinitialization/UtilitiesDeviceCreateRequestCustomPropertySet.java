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

@Component(name = UtilitiesDeviceCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        service = CustomPropertySet.class,
        property = "name=" + UtilitiesDeviceCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class UtilitiesDeviceCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, UtilitiesDeviceCreateRequestDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "UtilitiesDeviceCreateRequestCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceCreateRequestCustomPropertySet() {
    }

    @Inject
    public UtilitiesDeviceCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
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
        return UtilitiesDeviceCreateRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, UtilitiesDeviceCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .bigDecimalSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(), TranslationKeys.PARENT_SERVICE_CALL)
                        .describedAs(TranslationKeys.PARENT_SERVICE_CALL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .describedAs(TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SERIAL_ID.javaName(), TranslationKeys.SERIAL_ID)
                        .describedAs(TranslationKeys.SERIAL_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .describedAs(TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .describedAs(TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, UtilitiesDeviceCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_UD4";
        private final String FK = "FK_SAP_CPS_UD4";

        @Override
        public String componentName() {
            return "UD4";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<UtilitiesDeviceCreateRequestDomainExtension> persistenceClass() {
            return UtilitiesDeviceCreateRequestDomainExtension.class;
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
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                    .number()
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(80)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SERIAL_ID.databaseName())
                    .varChar(80)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SERIAL_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar(80)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(4000)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
