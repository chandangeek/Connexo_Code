package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
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
 * A versioned {@link CustomPropertySet} for the {@link TestDomain}
 * that will be used in the integration test classes of this bundle.
 * Should this be a real implementation, the class would need
 * a @Component annotation so that the OSGi container
 * would automatically pick it up and add it to the
 * {@link CustomPropertySet} whiteboard.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (14:28)
 */
public class VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes implements CustomPropertySet<TestDomain, VersionedDomainExtensionForTestingPurposes> {

    public static final String TABLE_NAME = "T05_CUSTOM_BILLING";
    public static final String FK_CUST_BILLING_DOMAIN = "FK_05CUST_BILLING_DOMAIN";

    private final PropertySpecService propertySpecService;

    public VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getName() {
        return VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes.class.getSimpleName();
    }

    @Override
    public Class<TestDomain> getDomainClass() {
        return TestDomain.class;
    }

    @Override
    public PersistenceSupport<TestDomain, VersionedDomainExtensionForTestingPurposes> getPersistenceSupport() {
        return new MyPeristenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return true;
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
        PropertySpec serviceCategoryPropertySpec = this.propertySpecService
                .specForValuesOf(new ServiceCategoryValueFactory())
                .named(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName())
                .describedAs("Description")
                .markRequired()
                .finish();
        PropertySpec billingCyclePropertySpec = this.propertySpecService
                .bigDecimalSpec()
                .named(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName())
                .describedAs("Descpription for DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName()")
                .markRequired()
                .setDefaultValue(BigDecimal.ONE)
                .finish();
        PropertySpec contractNumberPropertySpec = this.propertySpecService
                .stringSpec()
                .named(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName())
                .describedAs("Description for DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName()")
                .finish();
        return Arrays.asList(serviceCategoryPropertySpec, billingCyclePropertySpec, contractNumberPropertySpec);
    }

    private static class MyPeristenceSupport implements PersistenceSupport<TestDomain, VersionedDomainExtensionForTestingPurposes> {
        @Override
        public String componentName() {
            return "T05";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return VersionedDomainExtensionForTestingPurposes.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CUST_BILLING_DOMAIN;
        }

        @Override
        public Class<VersionedDomainExtensionForTestingPurposes> persistenceClass() {
            return VersionedDomainExtensionForTestingPurposes.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            Column serviceCategory = table
                    .column(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName())
                    .notNull()
                    .add();
            return Collections.singletonList(serviceCategory);
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                .column(VersionedDomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.databaseName())
                .number()
                .map(VersionedDomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName())
                .notNull()
                .add();
            table
                .column(VersionedDomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.databaseName())
                .varChar()
                .map(VersionedDomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName())
                .add();
        }
    }

}