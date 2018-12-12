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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointContCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointContDomainExtension> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "CON_CPS_USAGEPOINT_CONTRCT";
    public static final String FK_CPS_DEVICE_CONTRACTUAL = "FK_CPS_USAGEPOINT_CNTRCT";
    public static final String COMPONENT_NAME = "CON";

    public UsagePointContCustomPropertySet() {
        super();
    }

    public UsagePointContCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_CONTRACTUAL_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointContDomainExtension> getPersistenceSupport() {
        return new UsagePointConPersistenceSupport(this.getThesaurus());
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
        PropertySpec billingCycleSpec = propertySpecService
                .stringSpec()
                .named(UsagePointContDomainExtension.Fields.BILLING_CYCLE.javaName(), TranslationKeys.CPS_CONTRACTUAL_BILLING_CYCLE)
                .fromThesaurus(this.getThesaurus())
                .addValues("Monthly", "Yearly")
                .markRequired()
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        return Collections.singletonList(billingCycleSpec);
    }

    private static class UsagePointConPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointContDomainExtension> {

        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        private UsagePointConPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointContDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_CONTRACTUAL;
        }

        @Override
        public Class<UsagePointContDomainExtension> persistenceClass() {
            return UsagePointContDomainExtension.class;
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
            table.column(UsagePointContDomainExtension.Fields.BILLING_CYCLE.databaseName())
                    .varChar()
                    .map(UsagePointContDomainExtension.Fields.BILLING_CYCLE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointContDomainExtension.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointContDomainExtension.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }

}