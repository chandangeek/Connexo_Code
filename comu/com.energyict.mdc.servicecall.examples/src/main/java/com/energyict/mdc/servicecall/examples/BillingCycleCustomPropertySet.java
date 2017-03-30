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
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.servicecall.BillingCycleCustomPropertySet",
        service = CustomPropertySet.class,
        immediate = true)
public class BillingCycleCustomPropertySet implements CustomPropertySet<ServiceCall, BillingCycleDomainExtension> {

    static final String COMPONENT_NAME = "SC1";

    public BillingCycleCustomPropertySet() {
    }

    @Inject
    public BillingCycleCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService) {
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
        return BillingCycleCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, BillingCycleDomainExtension> getPersistenceSupport() {
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
        return Collections.singletonList(
                this.propertySpecService
                        .stringSpec()
                        .named(BillingCycleDomainExtension.FieldNames.BILLING_CYCLE.javaName(), BillingCycleDomainExtension.FieldNames.BILLING_CYCLE
                                .javaName())
                        .describedAs("Billing Cycle")
                        .setDefaultValue("No billing cycle")
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, BillingCycleDomainExtension> {
        private final String TABLE_NAME = "TVN_SCS_CPS_BC";
        private final String FK = "FK_SCS_CPS_BC";

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
            return BillingCycleDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<BillingCycleDomainExtension> persistenceClass() {
            return BillingCycleDomainExtension.class;
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
                .column(BillingCycleDomainExtension.FieldNames.BILLING_CYCLE.databaseName())
                .varChar()
                .map(BillingCycleDomainExtension.FieldNames.BILLING_CYCLE.javaName())
                .notNull()
                .add();
        }
    }

}