package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link CustomPropertySet} for the {@link TestDomain}
 * that will be used in the integration test classes of this bundle.
 * Should this be a real implementation, the class would need
 * a @Component annotation so that the OSGi container
 * would automatically pick it up and add it to the
 * {@link CustomPropertySet} whiteboard.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (14:28)
 */
public class CustomPropertySetForTestingPurposes implements CustomPropertySet<TestDomain, DomainExtensionForTestingPurposes> {

    private static final String TABLE_NAME = "T02_CUSTOM_BILLING";
    private static final String FK_CUST_BILLING_DOMAIN = "FK_02CUST_BILLING_DOMAIN";

    private final PropertySpecService propertySpecService;

    public CustomPropertySetForTestingPurposes(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return CustomPropertySetForTestingPurposes.class.getSimpleName();
    }

    @Override
    public String getName() {
        return CustomPropertySetForTestingPurposes.class.getSimpleName();
    }

    @Override
    public Class<TestDomain> getDomainClass() {
        return TestDomain.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getName();
    }

    @Override
    public PersistenceSupport<TestDomain, DomainExtensionForTestingPurposes> getPersistenceSupport() {
        return new MyPersistenceSupport();
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
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpec billingCyclePropertySpec = this.propertySpecService
                .bigDecimalSpec()
                .named(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName())
                .describedAs("Description")
                .markRequired()
                .setDefaultValue(BigDecimal.ONE)
                .finish();
        PropertySpec contractNumberPropertySpec = this.propertySpecService
                .stringSpec()
                .named(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName())
                .describedAs("Description")
                .finish();
        return Arrays.asList(billingCyclePropertySpec, contractNumberPropertySpec);
    }

    private static class MyPersistenceSupport implements PersistenceSupport<TestDomain, DomainExtensionForTestingPurposes> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "T06";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DomainExtensionForTestingPurposes.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CUST_BILLING_DOMAIN;
        }

        @Override
        public Class<DomainExtensionForTestingPurposes> persistenceClass() {
            return DomainExtensionForTestingPurposes.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            // None of the custom properties are part of the primary key
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                .column(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.databaseName())
                .number()
                .map(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName())
                .notNull()
                .add();
            table
                .column(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.databaseName())
                .varChar()
                .map(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName())
                .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(DomainExtensionForTestingPurposes.FieldNames.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(DomainExtensionForTestingPurposes.FieldNames::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

    }

}