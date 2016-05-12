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

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointTechElCPS implements CustomPropertySet<UsagePoint, UsagePointTechElDomExt> {

    public static final String TABLE_NAME = "RVK_CPS_TECH_EL";
    public static final String FK_CPS_DEVICE_ONE = "FK_CPS_TECH_EL";

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public UsagePointTechElCPS() {
        super();
    }

    public UsagePointTechElCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
        System.out.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return this.getThesaurus().
                getFormat(TranslationKeys.CPS_TECHNICAL_ELECTRICITY_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
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
                .stringSpec()
                .named(UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("EU", "NA")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        PropertySpec volatageLevelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechElDomExt.FieldNames.VOLTAGE_LEVEL.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_VOLTAGE_LEVEL)
                .fromThesaurus(this.getThesaurus())
                .addValues("low", "medium", "high")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();

        PropertySpec cableLocationSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointTechElDomExt.FieldNames.CABLE_LOCATION.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues(Quantity.create(new BigDecimal(0), 0, "m"),
                        Quantity.create(new BigDecimal(0), 3, "m"))
                .finish();

        return Arrays.asList(
                crossSectionalAreaSpec,
                volatageLevelSpec,
                cableLocationSpec);
    }

    private static class UsagePointTechnicalElectricityPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechElDomExt> {
        private Thesaurus thesaurus;

        public UsagePointTechnicalElectricityPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }


        @Override
        public String componentName() {
            return "TE1";
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
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA.databaseName())
                    .varChar(255)
                    .map(UsagePointTechElDomExt.FieldNames.CROSS_SECTIONAL_AREA.javaName())
                    .add();
            table.column(UsagePointTechElDomExt.FieldNames.VOLTAGE_LEVEL.databaseName())
                    .varChar(255)
                    .map(UsagePointTechElDomExt.FieldNames.VOLTAGE_LEVEL.javaName())
                    .add();
            table.addQuantityColumns(UsagePointTechElDomExt.FieldNames.CABLE_LOCATION.databaseName(), false, UsagePointTechElDomExt.FieldNames.CABLE_LOCATION
                    .javaName());

        }
    }
}