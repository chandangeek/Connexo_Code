/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.servicecall;

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

import com.energyict.mdc.processes.keyrenewal.api.KeyRenewalApplication;
import com.energyict.mdc.processes.keyrenewal.api.KeyRenewalChecklist;
import com.energyict.mdc.processes.keyrenewal.api.TranslationSeeds;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.servicecall.keyrenewal.customPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + KeyRenewalCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class KeyRenewalCustomPropertySet implements CustomPropertySet<ServiceCall, KeyRenewalDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "Key renewal";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    public KeyRenewalCustomPropertySet() {
    }

    @Inject
    public KeyRenewalCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        customPropertySetService.addCustomPropertySet(this);
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
        this.thesaurus = nlsService.getThesaurus(KeyRenewalApplication.COMPONENT_NAME, Layer.REST);
    }

    @Override
    public String getName() {
        return KeyRenewalCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationSeeds.CPS_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, KeyRenewalDomainExtension> getPersistenceSupport() {
        return new MyPersistenceSupport();
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
                        .named(KeyRenewalDomainExtension.FieldNames.CALLBACK_SUCCESS.javaName(), TranslationSeeds.CALL_BACK_SUCCESS_URL)
                        .describedAs(TranslationSeeds.CALL_BACK_SUCCESS_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(KeyRenewalDomainExtension.FieldNames.CALLBACK_ERROR.javaName(), TranslationSeeds.CALL_BACK_ERROR_URL)
                        .describedAs(TranslationSeeds.CALL_BACK_ERROR_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(KeyRenewalDomainExtension.FieldNames.PROVIDED_RESPONSE.javaName(), TranslationSeeds.PROVIDED_RESPONSE)
                        .describedAs(TranslationSeeds.PROVIDED_RESPONSE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, KeyRenewalDomainExtension> {
        private final String TABLE_NAME = "PKR_SCS_CNT";
        private final String FK = "FK_PKR_SCS_CNT";

        @Override
        public String componentName() {
            return "PKC";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return KeyRenewalDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<KeyRenewalDomainExtension> persistenceClass() {
            return KeyRenewalDomainExtension.class;
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
            table
                    .column(KeyRenewalDomainExtension.FieldNames.CALLBACK_SUCCESS.databaseName())
                    .varChar()
                    .map(KeyRenewalDomainExtension.FieldNames.CALLBACK_SUCCESS.javaName())
                    .notNull()
                    .add();
            table
                    .column(KeyRenewalDomainExtension.FieldNames.CALLBACK_ERROR.databaseName())
                    .varChar()
                    .map(KeyRenewalDomainExtension.FieldNames.CALLBACK_ERROR.javaName())
                    .notNull()
                    .add();
            table
                    .column(KeyRenewalDomainExtension.FieldNames.PROVIDED_RESPONSE.databaseName())
                    .bool()
                    .map(KeyRenewalDomainExtension.FieldNames.PROVIDED_RESPONSE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return KeyRenewalChecklist.APPLICATION_NAME;
        }
    }
}