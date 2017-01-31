/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class UsagePointTechElCPS implements CustomPropertySet<UsagePoint, UsagePointTechElDomExt> {

    public static final String COMPONENT_NAME = "TE1";
    public static final String TABLE_NAME = "TE1_CPS_TECH_EL";
    private static final String FK_CPS_DEVICE_ONE = "FK_TE1_TECH_EL";

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    UsagePointTechElCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_TECHNICAL_ELECTRICITY_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CPS_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechElDomExt> getPersistenceSupport() {
        return new UsagePointTechnicalElectricityPersistenceSupport(this.getThesaurus());
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
        PropertySpec crossSectionalAreaSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues(Quantity.create(BigDecimal.ZERO, -3, "m2"))
//                .stringSpec()
//                .named(UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA)
//                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA_DESCRIPTION)
//                .fromThesaurus(this.getThesaurus())
//                .addValues("x mm\u00b2")
//                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        PropertySpec volatageLevelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechElDomExt.FieldNames.VOLTAGE_LEVEL.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_VOLTAGE_LEVEL)
                .fromThesaurus(this.getThesaurus())
                .addValues("Low", "Medium", "High")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();

        PropertySpec cableLocationSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointTechElDomExt.FieldNames.CABLE_LOCATION.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues(
                        Quantity.create(BigDecimal.ZERO, 0, "m"),
                        Quantity.create(BigDecimal.ZERO, 3, "m"))
                .finish();

        return Arrays.asList(
                crossSectionalAreaSpec,
                volatageLevelSpec,
                cableLocationSpec);
    }

    private static class UsagePointTechnicalElectricityPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechElDomExt> {
        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointTechnicalElectricityPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
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
            return UsagePointTechElDomExt.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_ONE;
        }

        @Override
        public Class<UsagePointTechElDomExt> persistenceClass() {
            return UsagePointTechElDomExt.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.addQuantityColumns(UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA.databaseName(), false, UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA
                    .javaName());
            table.column(UsagePointTechElDomExt.FieldNames.VOLTAGE_LEVEL.databaseName())
                    .varChar()
                    .map(UsagePointTechElDomExt.FieldNames.VOLTAGE_LEVEL.javaName())
                    .add();
            table.addQuantityColumns(UsagePointTechElDomExt.FieldNames.CABLE_LOCATION.databaseName(), false, UsagePointTechElDomExt.FieldNames.CABLE_LOCATION
                    .javaName());

        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointTechElDomExt.FieldNames.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointTechElDomExt.FieldNames::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

    }

}