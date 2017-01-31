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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class UsagePointContrElectrCPS implements CustomPropertySet<UsagePoint, UsagePointContrElectrDomExt> {
    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "ELC_CPS_USAGEPOINT_EL_CON";
    private static final String FK_CPS_DEVICE_CONTR_ELECTRICITY = "FK_CPS_USAGEPOINT_EL_CON";
    public static final String COMPONENT_NAME = "ELC";

    UsagePointContrElectrCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return getThesaurus().getFormat(TranslationKeys.CPS_CUSTOM_CONTRACTUAL_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointContrElectrDomExt> getPersistenceSupport() {
        return new UsagePointContractualPerSupp(this.getThesaurus());
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
        PropertySpec contractedPowerSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointContrElectrDomExt.Fields.CONTRACTED_POWER.javaName(), TranslationKeys.CPS_CUSTOM_CONTRACTUAL_CONTRACTED_POWER)
                .fromThesaurus(this.getThesaurus())
                .addValues(
                        Quantity.create(BigDecimal.ZERO, 0, "W"),
                        Quantity.create(BigDecimal.ZERO, 3, "W"),
                        Quantity.create(BigDecimal.ZERO, 6, "W"),
                        Quantity.create(BigDecimal.ZERO, 9, "W"),
                        Quantity.create(BigDecimal.ZERO, 12, "W"))
                .finish();
        return Collections.singletonList(contractedPowerSpec);
    }

    private class UsagePointContractualPerSupp implements PersistenceSupport<UsagePoint, UsagePointContrElectrDomExt> {
        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointContractualPerSupp(Thesaurus thesaurus) {
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
            return UsagePointGeneralDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_CONTR_ELECTRICITY;
        }

        @Override
        public Class<UsagePointContrElectrDomExt> persistenceClass() {
            return UsagePointContrElectrDomExt.class;
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
            table.addQuantityColumns(
                    UsagePointContrElectrDomExt.Fields.CONTRACTED_POWER.databaseName(),
                    false,
                    UsagePointContrElectrDomExt.Fields.CONTRACTED_POWER.javaName());
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointContrElectrDomExt.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointContrElectrDomExt.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

    }

}