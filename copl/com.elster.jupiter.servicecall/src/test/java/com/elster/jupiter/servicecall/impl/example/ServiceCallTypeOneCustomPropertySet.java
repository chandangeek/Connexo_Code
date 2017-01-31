/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl.example;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
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
 * Created by bvn on 2/15/16.
 */
@Component(name = "com.elster.jupiter.ServcieCallTypeCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class ServiceCallTypeOneCustomPropertySet implements CustomPropertySet<ServiceCall, ServiceCallTypeDomainExtension> {

    public ServiceCallTypeOneCustomPropertySet() {
    }

    @Inject
    public ServiceCallTypeOneCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService) {
        this.propertySpecService = propertySpecService;
        customPropertySetService.addCustomPropertySet(this);
    }

    public volatile PropertySpecService propertySpecService;

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {

    }

    @Override
    public String getName() {
        return ServiceCallTypeOneCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getName();
    }

    @Override
    public PersistenceSupport<ServiceCall, ServiceCallTypeDomainExtension> getPersistenceSupport() {
        return new RegisterTypeFivePersistenceSupport();
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
                        .stringSpec()
                        .named(ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                        .describedAs("infoString")
                        .setDefaultValue("description")
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("flag")
                        .setDefaultValue(false)
                        .finish());
    }

    private class RegisterTypeFivePersistenceSupport implements PersistenceSupport<ServiceCall, ServiceCallTypeDomainExtension> {
        private final String TABLE_NAME = "BVN_CPS_EXAMPLE_CPS";
        private final String FK_CPS_EXAMPLE_CPS = "FK_CPS_EXAMPLE_CPS";

        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "OM1";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ServiceCallTypeDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_EXAMPLE_CPS;
        }

        @Override
        public Class<ServiceCallTypeDomainExtension> persistenceClass() {
            return ServiceCallTypeDomainExtension.class;
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
                    .column(ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                    .varChar()
                    .map(ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                    .notNull()
                    .add();
            table
                    .column(ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(ServiceCallTypeDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}
