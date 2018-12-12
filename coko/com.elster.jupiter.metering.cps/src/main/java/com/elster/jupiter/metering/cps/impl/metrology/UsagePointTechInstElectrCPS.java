/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.InsightServiceCategoryCustomPropertySetsCheckList;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
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

public class UsagePointTechInstElectrCPS implements CustomPropertySet<UsagePoint, UsagePointTechInstElectrDE> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "MIS_CPS_USAGEPOINT_TECH";
    private static final String FK_CPS_DEVICE_LICENSE = "FK_CPS_USAGEPOINT_TECHINS";
    public static final String COMPONENT_NAME = "MIS";

    public UsagePointTechInstElectrCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus()
                .getFormat(TranslationKeys.CPS_TECHNICAL_INSTALLATION_ELECTRICITY_SIMPLE_NAME)
                .format();
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
    public PersistenceSupport<UsagePoint, UsagePointTechInstElectrDE> getPersistenceSupport() {
        return new UsagePointTechInstElectyPerSupp(this.getThesaurus());
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
        PropertySpec distanceFromTheSubstationSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointTechInstElectrDE.Fields.SUBSTATION_DISTANCE.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_DISTANCE_FROM_THE_SUBSTATION)
                .describedAs(TranslationKeys.CPS_TECHNICAL_INSTALLATION_DISTANCE_FROM_THE_SUBSTATION_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(
                        Quantity.create(BigDecimal.ZERO, 0, "m"),
                        Quantity.create(BigDecimal.ZERO, 3, "m"))
                .finish();
        PropertySpec feederSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechInstElectrDE.Fields.FEEDER.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_FEEDER)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec utilizationCategorySpec = this.propertySpecService
                .stringSpec()
                .named(UsagePointTechInstElectrDE.Fields.UTILIZATION_CATEGORY.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_UTILIZATION_CATEGORY)
                .fromThesaurus(this.getThesaurus())
                .finish();
        return Arrays.asList(
                distanceFromTheSubstationSpec,
                feederSpec,
                utilizationCategorySpec);
    }

    private static class UsagePointTechInstElectyPerSupp implements PersistenceSupport<UsagePoint, UsagePointTechInstElectrDE> {

        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointTechInstElectyPerSupp(Thesaurus thesaurus) {
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
            return UsagePointTechInstElectrDE.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_LICENSE;
        }

        @Override
        public Class<UsagePointTechInstElectrDE> persistenceClass() {
            return UsagePointTechInstElectrDE.class;
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
            table.addQuantityColumns(UsagePointTechInstElectrDE.Fields.SUBSTATION_DISTANCE.databaseName(), true, UsagePointTechInstElectrDE.Fields.SUBSTATION_DISTANCE
                    .javaName());
            table.column(UsagePointTechInstElectrDE.Fields.FEEDER.databaseName())
                    .varChar()
                    .map(UsagePointTechInstElectrDE.Fields.FEEDER.javaName())
                    .add();
            table.column(UsagePointTechInstElectrDE.Fields.UTILIZATION_CATEGORY.databaseName())
                    .varChar()
                    .map(UsagePointTechInstElectrDE.Fields.UTILIZATION_CATEGORY.javaName())
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointTechInstElectrDE.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointTechInstElectrDE.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

    }

}