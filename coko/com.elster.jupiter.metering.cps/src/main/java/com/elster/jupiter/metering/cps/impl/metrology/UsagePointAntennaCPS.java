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
import com.elster.jupiter.orm.ColumnConversion;
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

public class UsagePointAntennaCPS implements CustomPropertySet<UsagePoint, UsagePointAntennaDomExt> {
    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "ANT_CPS_ANTENNA";
    public static final String DOMAIN_FK_NAME = "PK_CPS_ANTENNA_USAGEPOINT";
    public static final String COMPONENT_NAME = "ANT";

    public UsagePointAntennaCPS() {
        super();
    }

    public UsagePointAntennaCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus()
                .getFormat(TranslationKeys.CPS_ANTENNA_NAME)
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
    public PersistenceSupport<UsagePoint, UsagePointAntennaDomExt> getPersistenceSupport() {
        return new UsagePointAntennaPerSupp(this.getThesaurus());
    }

    @Override
    public boolean isRequired() {
        return true;
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
        PropertySpec antennaPower = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointAntennaDomExt.Fields.ANTENNA_POWER.javaName(), TranslationKeys.CPS_ANTENNA_POWER_NAME)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(
                        Quantity.create(BigDecimal.ZERO, -3, "Wh"),
                        Quantity.create(BigDecimal.ZERO, 0, "Wh"),
                        Quantity.create(BigDecimal.ZERO, 3, "Wh"))
                .finish();

        PropertySpec antennaCount = propertySpecService
                .longSpec()
                .named(UsagePointAntennaDomExt.Fields.ANTENNA_COUNT.javaName(), TranslationKeys.CPS_ANTENNA_COUNT_NAME)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();

        return Arrays.asList(
                antennaPower,
                antennaCount);
    }

    private static class UsagePointAntennaPerSupp implements PersistenceSupport<UsagePoint, UsagePointAntennaDomExt> {

        private Thesaurus thesaurus;

        private UsagePointAntennaPerSupp(Thesaurus thesaurus) {
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
            return UsagePointAntennaDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return DOMAIN_FK_NAME;
        }

        @Override
        public Class<UsagePointAntennaDomExt> persistenceClass() {
            return UsagePointAntennaDomExt.class;
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
            table.addQuantityColumns(UsagePointAntennaDomExt.Fields.ANTENNA_POWER.databaseName(), true, UsagePointAntennaDomExt.Fields.ANTENNA_POWER
                    .javaName());
            table.column(UsagePointAntennaDomExt.Fields.ANTENNA_COUNT.databaseName())
                    .number()
                    .map(UsagePointAntennaDomExt.Fields.ANTENNA_COUNT.javaName())
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointAntennaDomExt.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointAntennaDomExt.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }
    }
}
