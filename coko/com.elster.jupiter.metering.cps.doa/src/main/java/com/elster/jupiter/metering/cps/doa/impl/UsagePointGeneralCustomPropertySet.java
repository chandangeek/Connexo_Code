/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.metering.cps.doa.UsagePointGeneral",
        service = CustomPropertySet.class,
        property = "name=UsagePointGeneral",
        immediate = true)
@SuppressWarnings("unused")
public class UsagePointGeneralCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointGeneralDomainExtension> {

    private static final String TABLE_NAME = "DOA_USAGEPOINT_GENERAL";
    private static final String FK_CPS_DEVICE_GENERAL = "FK_DOA_USAGEPOINT_GENERAL";
    private static final String COMPONENT_NAME = "DOA";

    private PropertySpecService propertySpecService;

    @Reference
    @SuppressWarnings("unused") // Called by OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return "com.elster.jupiter.metering.cps.doa.UsagePointGeneral";
    }

    @Override
    public String getName() {
        return "General";
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointGeneralDomainExtension> getPersistenceSupport() {
        return new UsagePointGeneralPersistenceSupport();
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
        PropertySpec prepaySpec = propertySpecService
                .booleanSpec()
                .named(UsagePointGeneralDomainExtension.Fields.PREPAY.javaName(), "Prepay")
                .describedAs("For audit trail testing purposes only")
                .markRequired()
                .finish();
        PropertySpec marketCodeSectorSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.javaName(), "Market code sector")
                .describedAs("For audit trail testing purposes only")
                .addValues("Industrial & Commercial", "Domestic")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .markRequired()
                .finish();
        PropertySpec meteringPointTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.javaName(), "Metering point type")
                .describedAs("For audit trail testing purposes only")
                .addValues("Consumption", "Production", "Combined", "Exchange")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .markRequired()
                .finish();
        PropertySpec addedForAuditTrailTestingSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.ADDED_FOR_AUDIT_TRAIL_TESTING_PURPOSES.javaName(), "Belgium market type")
                .describedAs("For audit trail testing purposes only")
                .addValues("E17", "E18", "E19", "E20")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();

        return Arrays.asList(
                prepaySpec,
                marketCodeSectorSpec,
                meteringPointTypeSpec,
                addedForAuditTrailTestingSpec);
    }

    private class UsagePointGeneralPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointGeneralDomainExtension> {

        @Override
        public String application() {
            return COMPONENT_NAME;
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
            return UsagePointGeneralDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_GENERAL;
        }

        @Override
        public Class<UsagePointGeneralDomainExtension> persistenceClass() {
            return UsagePointGeneralDomainExtension.class;
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
            table.column(UsagePointGeneralDomainExtension.Fields.PREPAY.databaseName())
                    .bool()
                    .map(UsagePointGeneralDomainExtension.Fields.PREPAY.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.databaseName())
                    .varChar()
                    .map(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.databaseName())
                    .varChar()
                    .map(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointGeneralDomainExtension.Fields.ADDED_FOR_AUDIT_TRAIL_TESTING_PURPOSES.databaseName())
                    .varChar()
                    .map(UsagePointGeneralDomainExtension.Fields.ADDED_FOR_AUDIT_TRAIL_TESTING_PURPOSES.javaName())
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointGeneralDomainExtension.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointGeneralDomainExtension.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

    }

}