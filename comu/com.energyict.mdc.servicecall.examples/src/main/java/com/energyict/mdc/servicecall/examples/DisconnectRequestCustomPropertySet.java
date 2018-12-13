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
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.servicecall.DisconnectRequestCustomPropertySet",
        service = CustomPropertySet.class,
        immediate = true)
public class DisconnectRequestCustomPropertySet implements CustomPropertySet<ServiceCall, DisconnectRequestDomainExtension> {

    static final String COMPONENT_NAME = "SC2";

    @SuppressWarnings("unused") // For OSGi framework
    public DisconnectRequestCustomPropertySet() {
    }

    @Inject
    public DisconnectRequestCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService) {
        this();
        this.propertySpecService = propertySpecService;
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
        return DisconnectRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, DisconnectRequestDomainExtension> getPersistenceSupport() {
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
                        .named(
                            DisconnectRequestDomainExtension.FieldNames.REASON.javaName(),
                            DisconnectRequestDomainExtension.FieldNames.REASON.javaName())
                        .describedAs("Reason")
                        .setDefaultValue("no reason given")
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(
                            DisconnectRequestDomainExtension.FieldNames.ATTEMPTS.javaName(),
                            DisconnectRequestDomainExtension.FieldNames.ATTEMPTS.javaName())
                        .describedAs("Attempts")
                        .setDefaultValue(BigDecimal.ZERO)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(
                            DisconnectRequestDomainExtension.FieldNames.ENDDATE.javaName(),
                            DisconnectRequestDomainExtension.FieldNames.ENDDATE.javaName())
                        .describedAs("End date")
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, DisconnectRequestDomainExtension> {
        private final String TABLE_NAME = "TVN_SCS_CPS_DR";
        private final String FK = "FK_SCS_CPS_DR";

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
            return DisconnectRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<DisconnectRequestDomainExtension> persistenceClass() {
            return DisconnectRequestDomainExtension.class;
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
                .column(DisconnectRequestDomainExtension.FieldNames.REASON.databaseName())
                .varChar()
                .map(DisconnectRequestDomainExtension.FieldNames.REASON.javaName())
                .notNull()
                .add();
            table
                .column(DisconnectRequestDomainExtension.FieldNames.ATTEMPTS.databaseName())
                .number()
                .map(DisconnectRequestDomainExtension.FieldNames.ATTEMPTS.javaName())
                .notNull()
                .add();
            table
                .column(DisconnectRequestDomainExtension.FieldNames.ENDDATE.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(DisconnectRequestDomainExtension.FieldNames.ENDDATE.javaName())
                .notNull()
                .add();
        }
    }

}