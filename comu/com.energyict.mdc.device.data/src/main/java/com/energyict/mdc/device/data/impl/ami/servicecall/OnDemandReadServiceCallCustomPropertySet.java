/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

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
import com.energyict.mdc.device.data.DeviceDataServices;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.data.OnDemandReadServiceCallCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + OnDemandReadServiceCallCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class OnDemandReadServiceCallCustomPropertySet implements CustomPropertySet<ServiceCall, OnDemandReadServiceCallDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "OnDemandReadServiceCallCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;

    public OnDemandReadServiceCallCustomPropertySet() {
    }

    @Inject
    public OnDemandReadServiceCallCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setCustomPropertySetService(customPropertySetService);
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
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return OnDemandReadServiceCallCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(CustomPropertySetsTranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, OnDemandReadServiceCallDomainExtension> getPersistenceSupport() {
        return new OnDemandReadServiceCallPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
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
                        .named(OnDemandReadServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.javaName(), OnDemandReadServiceCallDomainExtension.FieldNames.EXPECTED_TASKS
                                .javaName())
                        .describedAs(OnDemandReadServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(OnDemandReadServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.javaName(), OnDemandReadServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS
                                .javaName())
                        .describedAs(OnDemandReadServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(OnDemandReadServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.javaName(), OnDemandReadServiceCallDomainExtension.FieldNames.COMPLETED_TASKS
                                .javaName())
                        .describedAs(OnDemandReadServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(OnDemandReadServiceCallDomainExtension.FieldNames.TRIGGERDATE.javaName(), OnDemandReadServiceCallDomainExtension.FieldNames.TRIGGERDATE
                                .javaName())
                        .describedAs(OnDemandReadServiceCallDomainExtension.FieldNames.TRIGGERDATE.javaName())
                        .finish()
        );
    }

    private class OnDemandReadServiceCallPersistenceSupport implements PersistenceSupport<ServiceCall, OnDemandReadServiceCallDomainExtension> {
        private final String TABLE_NAME = "DDC_CPS_SERVICECALL_READ";
        private final String FK_DDC_CPS_SC_READ = "FK_DDC_CPS_SC_READ";

        @Override
        public String componentName() {
            return "ODR";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return OnDemandReadServiceCallDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_DDC_CPS_SC_READ;
        }

        @Override
        public Class<OnDemandReadServiceCallDomainExtension> persistenceClass() {
            return OnDemandReadServiceCallDomainExtension.class;
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
                    .column(OnDemandReadServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.databaseName())
                    .number()
                    .map(OnDemandReadServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.javaName())
                    .notNull()
                    .add();
            table
                    .column(OnDemandReadServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.databaseName())
                    .number()
                    .map(OnDemandReadServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.javaName())
                    .notNull()
                    .add();
            table
                    .column(OnDemandReadServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.databaseName())
                    .number()
                    .map(OnDemandReadServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.javaName())
                    .notNull()
                    .add();
            table
                    .column(OnDemandReadServiceCallDomainExtension.FieldNames.TRIGGERDATE.databaseName())
                    .number()
                    .map(OnDemandReadServiceCallDomainExtension.FieldNames.TRIGGERDATE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }

}
