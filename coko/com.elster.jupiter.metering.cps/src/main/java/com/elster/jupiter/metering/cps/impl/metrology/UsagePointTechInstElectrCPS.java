package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

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

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_T_INS";
    public static final String FK_CPS_DEVICE_LICENSE = "FK_CPS_MTR_USAGEPOINT_T_INS";
    public static final String COMPONENT_NAME = "MTR_T_INS";

    public UsagePointTechInstElectrCPS() {
        super();
    }

    public UsagePointTechInstElectrCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
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
                .addValues(Quantity.create(new BigDecimal(0), 0, "m"),
                        Quantity.create(new BigDecimal(0), 3, "m"))
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
            return "Example";
        }

        public UsagePointTechInstElectyPerSupp(Thesaurus thesaurus) {
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
                    .varChar(255)
                    .map(UsagePointTechInstElectrDE.Fields.FEEDER.javaName())
                    .add();
            table.column(UsagePointTechInstElectrDE.Fields.UTILIZATION_CATEGORY.databaseName())
                    .varChar(255)
                    .map(UsagePointTechInstElectrDE.Fields.UTILIZATION_CATEGORY.javaName())
                    .add();
        }
    }
}
