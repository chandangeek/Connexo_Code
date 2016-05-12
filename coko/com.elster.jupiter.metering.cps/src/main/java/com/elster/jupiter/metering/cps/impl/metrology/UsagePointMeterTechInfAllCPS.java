package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointMeterTechInfAllCPS implements CustomPropertySet<UsagePoint, UsagePointMeterTechInfAllDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_T_IN";
    public static final String FK_CPS_DEVICE_METER_TECH_INFORM = "FK_CPS_MTR_USAGEPOINT_T_IN";
    public static final String COMPONENT_NAME = "TECH_INF";

    public UsagePointMeterTechInfAllCPS() {
        super();
    }

    public UsagePointMeterTechInfAllCPS(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
        return this.getThesaurus().getFormat(TranslationKeys.CPS_METER_TECH_INFORMATION_ALL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
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
                .addValues("CR -Credit", "MT -Mechanical Token", "ET -Electronic Token", "CM -Coin", "PP -Prepayment", "TH -Thrift", "U -Unknown", "NS - SMETS non-compliant", "S1 - SMETS Version 1", "S2 - SMETS Version 2")
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

        private Thesaurus thesaurus;

        public UsagePointMeterTechInfAllPersSupp(Thesaurus thesaurus) {
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
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(UsagePointMeterTechInfAllDomExt.Fields.METER_MECHANISM.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterTechInfAllDomExt.Fields.METER_MECHANISM.javaName())
                    .add();
            table.column(UsagePointMeterTechInfAllDomExt.Fields.METER_TYPE.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterTechInfAllDomExt.Fields.METER_TYPE.javaName())
                    .add();
        }
    }
}
