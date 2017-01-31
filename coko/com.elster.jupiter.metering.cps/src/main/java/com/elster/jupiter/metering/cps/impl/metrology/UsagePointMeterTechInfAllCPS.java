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
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointMeterTechInfAllCPS implements CustomPropertySet<UsagePoint, UsagePointMeterTechInfAllDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "TEC_CPS_USAGEPNT_TECHNICAL";
    private static final String FK_CPS_DEVICE_METER_TECH_INFORM = "FK_CPS_USAGEPNT_TECHNICAL";
    public static final String COMPONENT_NAME = "TEC";

    public UsagePointMeterTechInfAllCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_METER_TECH_INFORMATION_ALL_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointMeterTechInfAllDomExt> getPersistenceSupport() {
        return new UsagePointMeterTechInfAllPersSupp(this.getThesaurus());
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
        PropertySpec meterMechanismSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfAllDomExt.Fields.METER_MECHANISM.javaName(), TranslationKeys.CPS_METER_TECH_MECHANISM)
                .describedAs(TranslationKeys.CPS_METER_TECH_MECHANISM_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("CR - Credit", "MT - Mechanical Token", "ET - Electronic Token", "CM - Coin", "PP - Prepayment", "TH - Thrift", "U - Unknown", "NS - SMETS non-compliant", "S1 - SMETS Version 1", "S2 - SMETS Version 2")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        PropertySpec meterTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfAllDomExt.Fields.METER_TYPE.javaName(), TranslationKeys.CPS_METER_TECH_TYPE)
                .describedAs(TranslationKeys.CPS_METER_TECH_TYPE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("R = Rotary", "T = Turbine", "D = Diaphragm of unknown material", "L = Leather Diaphragm", "S = Synthetic Diaphragm", "U = Ultrasonic", "Z = Unknown")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();

        return Arrays.asList(meterMechanismSpec,
                meterTypeSpec);
    }

    private static class UsagePointMeterTechInfAllPersSupp implements PersistenceSupport<UsagePoint, UsagePointMeterTechInfAllDomExt> {
        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        private Thesaurus thesaurus;

        UsagePointMeterTechInfAllPersSupp(Thesaurus thesaurus) {
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
            return UsagePointMeterTechInfAllDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_METER_TECH_INFORM;
        }

        @Override
        public Class<UsagePointMeterTechInfAllDomExt> persistenceClass() {
            return UsagePointMeterTechInfAllDomExt.class;
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
            table.column(UsagePointMeterTechInfAllDomExt.Fields.METER_MECHANISM.databaseName())
                    .varChar()
                    .map(UsagePointMeterTechInfAllDomExt.Fields.METER_MECHANISM.javaName())
                    .add();
            table.column(UsagePointMeterTechInfAllDomExt.Fields.METER_TYPE.databaseName())
                    .varChar()
                    .map(UsagePointMeterTechInfAllDomExt.Fields.METER_TYPE.javaName())
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointMeterTechInfAllDomExt.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointMeterTechInfAllDomExt.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }
}
