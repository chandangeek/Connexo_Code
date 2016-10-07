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
import com.elster.jupiter.properties.InstantFactory;
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

public class UsagePointDecentProdCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointDecentProdDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "DEC_CPS_USAGEPOINT_DECPROD";
    private static final String FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION = "FK_CPS_USAGEPOINT_DECPROD";
    public static final String COMPONENT_NAME = "DEC";

    public UsagePointDecentProdCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointDecentProdDomExt> getPersistenceSupport() {
        return new UsagePointDecentrProdPS(this.getThesaurus());
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
        PropertySpec installedPowerSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointDecentProdDomExt.Fields.INS_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_INSTALLED_POWER)
                .fromThesaurus(this.getThesaurus())
                .markEditable()
                .markRequired()
                .addValues(
                        Quantity.create(BigDecimal.ZERO, 0, "W"),
                        Quantity.create(BigDecimal.ZERO, 3, "W"),
                        Quantity.create(BigDecimal.ZERO, 6, "W"),
                        Quantity.create(BigDecimal.ZERO, 9, "W"),
                        Quantity.create(BigDecimal.ZERO, 12, "W"))
                .finish();
        PropertySpec convertorPowerSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_CONVERTER_POWER)
                .fromThesaurus(this.getThesaurus())
                .markEditable()
                .markRequired()
                .addValues(
                        Quantity.create(BigDecimal.ZERO, 0, "W"),
                        Quantity.create(BigDecimal.ZERO, 3, "W"),
                        Quantity.create(BigDecimal.ZERO, 6, "W"),
                        Quantity.create(BigDecimal.ZERO, 9, "W"),
                        Quantity.create(BigDecimal.ZERO, 12, "W"))
                .finish();
        PropertySpec typeOfDecentralizedProductionSpec = propertySpecService
                .stringSpec()
                .named(UsagePointDecentProdDomExt.Fields.DEC_PROD.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_TYPE_OF_DECENTRALIZED_PRODUCTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Solar", "Wind", "Other")
                .markRequired()
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        PropertySpec commissioningDateSpec = propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_COMMISSIONING_DATE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();

        return Arrays.asList(
                installedPowerSpec,
                convertorPowerSpec,
                typeOfDecentralizedProductionSpec,
                commissioningDateSpec);
    }

    private static class UsagePointDecentrProdPS implements PersistenceSupport<UsagePoint, UsagePointDecentProdDomExt> {
        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointDecentrProdPS(Thesaurus thesaurus) {
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
            return UsagePointDecentProdDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION;
        }

        @Override
        public Class<UsagePointDecentProdDomExt> persistenceClass() {
            return UsagePointDecentProdDomExt.class;
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
            table.addQuantityColumns(UsagePointDecentProdDomExt.Fields.INS_POWER.databaseName(), true,
                    UsagePointDecentProdDomExt.Fields.INS_POWER.javaName());
            table.addQuantityColumns(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.databaseName(), true,
                    UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.javaName());
            table.column(UsagePointDecentProdDomExt.Fields.DEC_PROD.databaseName())
                    .varChar()
                    .map(UsagePointDecentProdDomExt.Fields.DEC_PROD.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointDecentProdDomExt.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointDecentProdDomExt.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }
}
