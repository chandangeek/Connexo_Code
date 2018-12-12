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

public class UsagePointMeterGnrCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointMeterGnrDomainExtension> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "MET_CPS_USAGEPOINTMETGNRL";
    public static final String FK_CPS_DEVICE_MTR_GEN = "FK_CPS_USAGEPNTMETGNRL";
    public static final String COMPONENT_NAME = "MET";

    public UsagePointMeterGnrCustomPropertySet() {
        super();
    }

    public UsagePointMeterGnrCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_METER_GENERAL_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointMeterGnrDomainExtension> getPersistenceSupport() {
        return new UsagePointMtrGeneralPersistSupp(this.getThesaurus());
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
        PropertySpec manufacturerSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGnrDomainExtension.Fields.MANUFACTURER.javaName(), TranslationKeys.CPS_METER_GENERAL_MANUFACTURER)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_MANUFACTURER_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec modelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGnrDomainExtension.Fields.MODEL.javaName(), TranslationKeys.CPS_METER_GENERAL_MODEL)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_MODEL_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec classSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGnrDomainExtension.Fields.CLAZZ.javaName(), TranslationKeys.CPS_METER_GENERAL_CLAZZ)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_CLAZZ_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Class 0.5", "Class 1", "Class 2", "Class 3")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        return Arrays.asList(manufacturerSpec,
                modelSpec,
                classSpec);
    }

    private static class UsagePointMtrGeneralPersistSupp implements PersistenceSupport<UsagePoint, UsagePointMeterGnrDomainExtension> {
        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointMtrGeneralPersistSupp(Thesaurus thesaurus) {
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
            return UsagePointMeterGnrDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_MTR_GEN;
        }

        public Class<UsagePointMeterGnrDomainExtension> persistenceClass() {
            return UsagePointMeterGnrDomainExtension.class;
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
            table.column(UsagePointMeterGnrDomainExtension.Fields.MANUFACTURER.databaseName())
                    .varChar()
                    .map(UsagePointMeterGnrDomainExtension.Fields.MANUFACTURER.javaName())
                    .add();
            table.column(UsagePointMeterGnrDomainExtension.Fields.MODEL.databaseName())
                    .varChar()
                    .map(UsagePointMeterGnrDomainExtension.Fields.MODEL.javaName())
                    .add();
            table.column(UsagePointMeterGnrDomainExtension.Fields.CLAZZ.databaseName())
                    .varChar()
                    .map(UsagePointMeterGnrDomainExtension.Fields.CLAZZ.javaName())
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointMeterGnrDomainExtension.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointMeterGnrDomainExtension.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }
}
