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

import com.google.inject.Module;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class UsagePointGeneralCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointGeneralDomainExtension> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "MTC_CPS_USAGEPOINT_GENER";
    private static final String FK_CPS_DEVICE_GENERAL = "FK_CPS_USAGEPOINT_GENER";
    public static final String COMPONENT_NAME = "GEN";

    UsagePointGeneralCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus(){
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return getThesaurus().getFormat(TranslationKeys.CPS_GENERAL_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointGeneralDomainExtension> getPersistenceSupport() {
        return new UsagePointGeneralPersistenceSupport();
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
        PropertySpec prepaySpec = propertySpecService
                .booleanSpec()
                .named(UsagePointGeneralDomainExtension.Fields.PREPAY.javaName(), TranslationKeys.CPS_GENERAL_PROPERTIES_PREPAY)
                .describedAs(TranslationKeys.CPS_GENERAL_PROPERTIES_PREPAY_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();
        PropertySpec marketCodeSectorSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.javaName(), TranslationKeys.CPS_GENERAL_PROPERTIES_MARKET_CODE_SECTOR)
                .describedAs(TranslationKeys.CPS_GENERAL_PROPERTIES_MARKED_CODE_SECTOR_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Industrial & Commercial", "Domestic")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .markRequired()
                .finish();
        PropertySpec meteringPointTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.javaName(), TranslationKeys.CPS_GENERAL_PROPERTIES_METERING_POINT_TYPE)
                .describedAs(TranslationKeys.CPS_GENERAL_PROPERTIES_METERING_POINT_TYPE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("E17 - Consumption", "E18 - Production", "E19 - Combined", "E20 - Exchange")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .markRequired()
                .finish();

        return Arrays.asList(prepaySpec,
                marketCodeSectorSpec,
                meteringPointTypeSpec);
    }

    private class UsagePointGeneralPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointGeneralDomainExtension> {

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointGeneralPersistenceSupport() {
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