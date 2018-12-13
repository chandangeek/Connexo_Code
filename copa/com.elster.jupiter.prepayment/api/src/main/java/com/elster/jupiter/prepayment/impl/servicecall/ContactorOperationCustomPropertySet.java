/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl.servicecall;

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
import com.elster.jupiter.prepayment.impl.PrepaymentApplication;
import com.elster.jupiter.prepayment.impl.PrepaymentChecklist;
import com.elster.jupiter.prepayment.impl.TranslationSeeds;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author sva
 * @since 30/03/2016 - 15:43
 */
@Component(name = "com.energyict.servicecall.redknee.contactoroperation.customPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + ContactorOperationCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class ContactorOperationCustomPropertySet implements CustomPropertySet<ServiceCall, ContactorOperationDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "contactoroperation";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    public ContactorOperationCustomPropertySet() {
    }

    @Inject
    public ContactorOperationCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
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
        this.thesaurus = nlsService.getThesaurus(PrepaymentApplication.COMPONENT_NAME, Layer.REST);
    }

    @Override
    public String getName() {
        return ContactorOperationCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, ContactorOperationDomainExtension> getPersistenceSupport() {
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
                        .named(ContactorOperationDomainExtension.FieldNames.BREAKER_STATUS.javaName(), TranslationSeeds.BREAKER_STATUS)
                        .describedAs(TranslationSeeds.BREAKER_STATUS)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ContactorOperationDomainExtension.FieldNames.CALLBACK.javaName(), TranslationSeeds.CALL_BACK_URL)
                        .describedAs(TranslationSeeds.CALL_BACK_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(ContactorOperationDomainExtension.FieldNames.PROVIDED_RESPONSE.javaName(), TranslationSeeds.PROVIDED_RESPONSE)
                        .describedAs(TranslationSeeds.PROVIDED_RESPONSE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, ContactorOperationDomainExtension> {
        private final String TABLE_NAME = "RKN_SCS_CNT";
        private final String FK = "FK_RKN_SCS_CNT";

        @Override
        public String componentName() {
            return "RKP";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ContactorOperationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<ContactorOperationDomainExtension> persistenceClass() {
            return ContactorOperationDomainExtension.class;
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
                    .column(ContactorOperationDomainExtension.FieldNames.BREAKER_STATUS.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.BREAKER_STATUS.javaName())
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.CALLBACK.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.CALLBACK.javaName())
                    .notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.PROVIDED_RESPONSE.databaseName())
                    .bool()
                    .map(ContactorOperationDomainExtension.FieldNames.PROVIDED_RESPONSE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return PrepaymentChecklist.APPLICATION_NAME;
        }
    }
}