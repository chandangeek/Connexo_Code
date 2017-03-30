/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
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
@Component(name = "com.energyict.servicecall.devicegroup.certification.customPropertySet",
        service = CustomPropertySet.class,
        immediate = true)
public class DeviceGroupCertificationCustomPropertySet implements CustomPropertySet<ServiceCall, DeviceGroupCertificationDomainExtension> {

    static final String COMPONENT_NAME = "SC3";

    @SuppressWarnings("unused") // For OSGi framework
    public DeviceGroupCertificationCustomPropertySet() {
    }

    @Inject
    public DeviceGroupCertificationCustomPropertySet(CustomPropertySetService customPropertySetService) {
        this();
        customPropertySetService.addCustomPropertySet(this);
    }

    private volatile PropertySpecService propertySpecService;

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
    public void setCheckList(ServiceCallExampleCheckList checkList) {
        // Ensure that this component waits for the check list to be activated
    }

    @Override
    public String getName() {
        return DeviceGroupCertificationCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return "Service call";
    }

    @Override
    public PersistenceSupport<ServiceCall, DeviceGroupCertificationDomainExtension> getPersistenceSupport() {
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
                        .longSpec()
                        .named(
                            DeviceGroupCertificationDomainExtension.FieldNames.DEVICE_GROUP_ID.javaName(),
                            DeviceGroupCertificationDomainExtension.FieldNames.DEVICE_GROUP_ID.javaName())
                        .describedAs("Device group id")
                        .setDefaultValue(-1L)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(
                            DeviceGroupCertificationDomainExtension.FieldNames.YEAR_OF_CERTIFICATION.javaName(),
                            DeviceGroupCertificationDomainExtension.FieldNames.YEAR_OF_CERTIFICATION.javaName())
                        .describedAs("Year of certification")
                        .setDefaultValue(-1L)
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, DeviceGroupCertificationDomainExtension> {
        private final String TABLE_NAME = "SC3_CPS_DEVGRP_CER";
        private final String FK = "FK_SC3_CPS_DEVGRP_CER";

        @Override
        public String application() {
            return ServiceCallExampleCheckList.APPLICATION_NAME;
        }

        @Override
        public String componentName() {
            return COMPONENT_NAME;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceGroupCertificationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<DeviceGroupCertificationDomainExtension> persistenceClass() {
            return DeviceGroupCertificationDomainExtension.class;
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
                    .column(DeviceGroupCertificationDomainExtension.FieldNames.DEVICE_GROUP_ID.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(DeviceGroupCertificationDomainExtension.FieldNames.DEVICE_GROUP_ID.javaName())
                    .notNull()
                    .add();
            table
                    .column(DeviceGroupCertificationDomainExtension.FieldNames.YEAR_OF_CERTIFICATION.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(DeviceGroupCertificationDomainExtension.FieldNames.YEAR_OF_CERTIFICATION.javaName())
                    .notNull()
                    .add();
        }
    }
}
