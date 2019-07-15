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
import com.elster.jupiter.orm.ColumnConversion;
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

@Component(name = SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        service = CustomPropertySet.class,
        property = "name=" + SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet() {
    }

    @Inject
    public SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
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
        return SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(), TranslationKeys.PARENT_SERVICE_CALL)
                        .describedAs(TranslationKeys.PARENT_SERVICE_CALL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .describedAs(TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.RETURN_CODE.javaName(), TranslationKeys.RETURN_CODE)
                        .describedAs(TranslationKeys.RETURN_CODE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_UD2";
        private final String FK = "FK_SAP_CPS_UD2";

        @Override
        public String componentName() {
            return "UD2";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension> persistenceClass() {
            return SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class;
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
            table.column(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                    .number()
                    .map(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                    .notNull()
                    .add();
            table.column(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(80)
                    .map(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.RETURN_CODE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.RETURN_CODE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
