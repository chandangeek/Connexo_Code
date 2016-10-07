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

public class UsagePointMeterTechInfGTWCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointMeterTechInfGTWDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "INF_CPS_USAGEPNT_TECHINFO";
    private static final String FK_CPS_DEVICE_METER_TECH_INFORM = "FK_CPS_USAGEPNT_TECHINFO";
    public static final String COMPONENT_NAME = "INF";

    public UsagePointMeterTechInfGTWCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
                .getFormat(TranslationKeys.CPS_METER_TECH_INFORMATION_GTW_SIMPLE_NAME).format();
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
    public PersistenceSupport<UsagePoint, UsagePointMeterTechInfGTWDomExt> getPersistenceSupport() {
        return new UsagePointMeterTechInfGTWPersSupp(this.getThesaurus());
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
        PropertySpec recessedLengthSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfGTWDomExt.Fields.RECESSED_LENGTH.javaName(), TranslationKeys.CPS_METER_TECH_RECESSED_LENGTH)
                .describedAs(TranslationKeys.CPS_METER_TECH_RECESSED_LENGTH_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec connectionTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfGTWDomExt.Fields.CONNECTION_TYPE.javaName(), TranslationKeys.CPS_METER_TECH_CONNECTION_TYPE)
                .describedAs(TranslationKeys.CPS_METER_TECH_CONNECTION_TYPE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec conversionMetrologySpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfGTWDomExt.Fields.CONVERSION_METROLOGY.javaName(), TranslationKeys.CPS_METER_TECH_CONVERSION_METROLOGY)
                .describedAs(TranslationKeys.CPS_METER_TECH_CONVERSION_METROLOGY_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec pressureMaximalSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointMeterTechInfGTWDomExt.Fields.PRESSURE_MAX.javaName(), TranslationKeys.CPS_METER_TECH_PRESSURE_MAXIMAL)
                .describedAs(TranslationKeys.CPS_METER_TECH_PRESSURE_MAXIMAL_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues(Quantity.create(BigDecimal.ZERO, 1, "bar"))
                .finish();
        return Arrays.asList(recessedLengthSpec,
                connectionTypeSpec,
                conversionMetrologySpec,
                pressureMaximalSpec);
    }

    private static class UsagePointMeterTechInfGTWPersSupp implements PersistenceSupport<UsagePoint, UsagePointMeterTechInfGTWDomExt> {

        private Thesaurus thesaurus;

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }

        UsagePointMeterTechInfGTWPersSupp(Thesaurus thesaurus) {
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
            return UsagePointMeterTechInfGTWDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_METER_TECH_INFORM;
        }

        @Override
        public Class<UsagePointMeterTechInfGTWDomExt> persistenceClass() {
            return UsagePointMeterTechInfGTWDomExt.class;
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
            table.column(UsagePointMeterTechInfGTWDomExt.Fields.RECESSED_LENGTH.databaseName())
                    .varChar()
                    .map(UsagePointMeterTechInfGTWDomExt.Fields.RECESSED_LENGTH.javaName())
                    .add();
            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CONNECTION_TYPE.databaseName())
                    .varChar()
                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CONNECTION_TYPE.javaName())
                    .add();
            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CONVERSION_METROLOGY.databaseName())
                    .varChar()
                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CONVERSION_METROLOGY.javaName())
                    .add();
            table.addQuantityColumns(UsagePointMeterTechInfGTWDomExt.Fields.PRESSURE_MAX.databaseName(), false,
                    UsagePointMeterTechInfGTWDomExt.Fields.PRESSURE_MAX.javaName());
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointMeterTechInfGTWDomExt.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointMeterTechInfGTWDomExt.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }

}